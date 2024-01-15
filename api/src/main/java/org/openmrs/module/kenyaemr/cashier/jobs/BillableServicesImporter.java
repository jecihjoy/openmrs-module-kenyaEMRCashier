package org.openmrs.module.kenyaemr.cashier.jobs;

import liquibase.util.csv.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.cashier.api.IBillableItemsService;
import org.openmrs.module.kenyaemr.cashier.api.IPaymentModeService;
import org.openmrs.module.kenyaemr.cashier.api.model.BillableService;
import org.openmrs.module.kenyaemr.cashier.api.model.BillableServiceStatus;
import org.openmrs.module.kenyaemr.cashier.api.model.CashierItemPrice;
import org.openmrs.module.kenyaemr.cashier.api.model.PaymentMode;
import org.openmrs.module.kenyaemr.cashier.api.search.PaymentModeSearch;
import org.openmrs.module.stockmanagement.api.StockManagementService;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BillableServicesImporter {
    private Path file;
    private int SERVICE_NAME = 0;
    private int SERVICE_SHORT_NAME = 1;
    private int SERVICE_TYPE = 2;
    private int SERVICE_CATEGORY = 3;
    private int SERVICE_PAYMENT_METHOD = 4;
    private int SERVICE_PAYMENT_METHOD_PRICE = 5;

    private boolean hasHeader;

    private ImportResult result = new ImportResult();

    private int batchSize = 50;

    private boolean isBlank(String value) {
        return StringUtils.isBlank(value) || value.toLowerCase().equals("null");
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        if (batchSize > 0) {
            this.batchSize = batchSize;
        }
    }

    public BillableServicesImporter(Path file, boolean hasHeader) {
        this.file = file;
        this.hasHeader = hasHeader;
        result.setErrors(new ArrayList<>());
    }

    private Object validateLine(String[] line) {
        if (line == null || line.length == 0) return null;
        Object[] objects = new Object[6];
        List<String> errors = new ArrayList<>();
        if (line.length < 3) {
            errors.add(Context.getMessageSourceService().getMessage("kenyaemrcashier.importoperation.minimumfields"));
            return errors;
        }
        if (line.length > SERVICE_NAME && !isBlank(line[SERVICE_NAME])) {
            objects[SERVICE_NAME] = line[SERVICE_NAME];
        }

        if (line.length > SERVICE_SHORT_NAME && !isBlank(line[SERVICE_SHORT_NAME])) {
            objects[SERVICE_SHORT_NAME] = line[SERVICE_SHORT_NAME];
        }

        if (line.length > SERVICE_TYPE && !isBlank(line[SERVICE_TYPE])) {
            objects[SERVICE_TYPE] = line[SERVICE_TYPE].split("-")[1];
        }

        if (line.length > SERVICE_CATEGORY && !isBlank(line[SERVICE_CATEGORY])) {
            objects[SERVICE_CATEGORY] = line[SERVICE_CATEGORY].split("-")[1];
        }
        if (line.length > SERVICE_PAYMENT_METHOD && !isBlank(line[SERVICE_PAYMENT_METHOD])) {
            objects[SERVICE_PAYMENT_METHOD] = line[SERVICE_PAYMENT_METHOD];
        }

        if (line.length > SERVICE_PAYMENT_METHOD_PRICE && !isBlank(line[SERVICE_PAYMENT_METHOD_PRICE])) {
            objects[SERVICE_PAYMENT_METHOD_PRICE] = line[SERVICE_PAYMENT_METHOD_PRICE];
        }

        return errors.isEmpty() ? objects : errors;
    }

    @SuppressWarnings({"unchecked"})
    public void execute() {
        CSVReader csvReader = null;
        int row = 0;
        boolean hasErrors = false;
        try {
            try (Writer writer = Files.newBufferedWriter(new File(file.toString() + "_errors").toPath())) {
                boolean resetErrors = false;
                try (Reader reader = Files.newBufferedReader(file)) {
                    csvReader = new CSVReader(reader, ',', '\"', hasHeader ? 1 : 0);
                    String[] csvLine = null;
                    boolean processedPending = false;
                    Map<Integer, Object[]> list = new HashMap<>();
                    while ((csvLine = csvReader.readNext()) != null) {
                        row++;
                        processedPending = false;
                        resetErrors = false;
                        if (result.getErrors().size() > 10) {
                            hasErrors = true;
                            for (String error : result.getErrors()) {
                                writer.append(error);
                                writer.append("\r\n");
                            }
                            result.getErrors().clear();
                        }
                        Object validationResult = validateLine(csvLine);
                        if (validationResult == null) {
                            continue;
                        } else if (validationResult instanceof List<?>) {
                            List<String> errors = (List<String>) validationResult;
                            result.getErrors().add(String.format("Row %1s: %2s", row, String.join(", ", errors)));
                            continue;
                        }

                        list.put(row, (Object[]) validationResult);
                        if (list.size() == getBatchSize()) {
                            processUpload(list);
                            processedPending = true;
                            list.clear();
                        }

                    }
                    if (!processedPending) {
                        processUpload(list);
                    }
                }
                if (hasErrors) {
                    for (String error : result.getErrors()) {
                        writer.append(error);
                        writer.append("\r\n");
                    }
                    result.getErrors().clear();
                    result.getErrors().add(Context.getMessageSourceService().getMessage("kenyaemrcashier.importoperation.errorswhileimporting"));
                    result.setHasErrorFile(true);
                } else if (result.getErrors().isEmpty()) {
                    result.setSuccess(true);
                }
            }
        } catch (Exception exception) {
            result.getErrors().add(0, "Stopped processing at row " + Integer.toString(row));
            result.getErrors().add(exception.toString());
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (Exception exception) {
                }
            }
        }
    }

    private void processUpload(Map<Integer, Object[]> services) {
        IBillableItemsService billableItemsService = Context.getService(IBillableItemsService.class);

        List<Map.Entry<Integer, Object[]>> newStockItems = new ArrayList<>(services.entrySet());
        List<Map.Entry<Integer, Object[]>> rowsToCreate = new ArrayList<>();

        if (!newStockItems.isEmpty()) {
            // validate minimum information required to be created.
            for (Map.Entry<Integer, Object[]> entryToCreate : newStockItems) {
                List<String> errors = new ArrayList<>();
                if (entryToCreate.getValue()[SERVICE_NAME] == null) {
                    errors.add("Service name must be provided");
                }
                if (entryToCreate.getValue()[SERVICE_TYPE] == null) {
                    errors.add("Service Type must be provided");
                }
                if (!errors.isEmpty()) {
                    result.getErrors().add(String.format("Row %1s: %2s", entryToCreate.getKey(), String.join(", ", errors)));
                    continue;
                }
                rowsToCreate.add(entryToCreate);
            }
        }

        // prefetch the concepts and drugs
        List<Integer> conceptIds = services.entrySet().stream().map(p -> Arrays.asList(
                p.getValue()[SERVICE_TYPE],
                p.getValue()[SERVICE_CATEGORY]
        )).flatMap(Collection::stream).filter(p -> p != null).map(p -> (Integer) p).distinct().collect(Collectors.toList());

        Map<Integer, List<Concept>> conceptsMap = Context.getService(StockManagementService.class)
                .getConcepts(conceptIds).stream().collect(Collectors.groupingBy(Concept::getConceptId));

        for (Map.Entry<Integer, Object[]> recordToProcess : services.entrySet()) {
            Object[] valuesToProcess = null;

            Optional<Map.Entry<Integer, Object[]>> rowFound = rowsToCreate.stream().filter(p -> p.getKey().equals(recordToProcess.getKey())).findFirst();
            if (rowFound.isPresent()) {
                valuesToProcess = rowFound.get().getValue();
            }
            if (valuesToProcess == null) {
                continue;
            }

            BillableService service = new BillableService();
            Object[] updates = valuesToProcess;
            if (updates[SERVICE_NAME] != null) {
                List<Concept> serviceType = conceptsMap.getOrDefault(updates[SERVICE_TYPE], null);
                List<Concept> serviceCategory = conceptsMap.getOrDefault(updates[SERVICE_TYPE], null);
                if (serviceType == null || serviceType.isEmpty()) {
                    result.getErrors().add(String.format("Row %1s: %2s", recordToProcess.getKey(),
                            String.format(Context.getMessageSourceService().getMessage("stockmanagement.importoperation.drugnofound"),
                                    updates[SERVICE_TYPE].toString())));
                    continue;
                }
                service.setName((String) updates[SERVICE_NAME]);
                service.setShortName((String) updates[SERVICE_SHORT_NAME]);
                service.setServiceType(serviceType.get(0));
                if (serviceCategory != null && !serviceCategory.isEmpty()) {
                    service.setServiceType(serviceCategory.get(0));
                }
                service.setServiceStatus(BillableServiceStatus.ENABLED);
                service.setCreator(Context.getAuthenticatedUser());
                service.setDateCreated(new Date());

                PaymentMode searchTemplate = new PaymentMode();
                searchTemplate.setName((String) updates[SERVICE_PAYMENT_METHOD]);

                List<PaymentMode> method = Context.getService(IPaymentModeService.class).findPaymentModeByName(new PaymentModeSearch(searchTemplate));
                if (!method.isEmpty()) {
                    System.out.println("the method " + method.get(0).getUuid());
                    CashierItemPrice price = new CashierItemPrice();
                    price.setPaymentMode(method.get(0));
                    price.setName(method.get(0).getName());
                    price.setPrice(new BigDecimal((String) updates[SERVICE_PAYMENT_METHOD_PRICE]));

                    service.addServicePrice(price);
                }

                BillableService persisted = billableItemsService.save(service);
                if (persisted != null) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                }
                //                if (updates[SERVICE_PAYMENT_METHOD])
            } else {
                continue;
            }

        }
    }

    public Object getResult() {
        return result;
    }
}

