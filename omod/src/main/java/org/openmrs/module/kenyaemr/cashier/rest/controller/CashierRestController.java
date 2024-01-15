package org.openmrs.module.kenyaemr.cashier.rest.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.cashier.api.IBillableItemsService;
import org.openmrs.module.kenyaemr.cashier.api.model.BillableService;
import org.openmrs.module.kenyaemr.cashier.jobs.ImportResult;
import org.openmrs.module.kenyaemr.cashier.rest.controller.base.CashierResourceController;
import org.openmrs.module.kenyaemr.cashier.rest.restmapper.BillableServiceMapper;
import org.openmrs.module.stockmanagement.api.utils.FileUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + CashierResourceController.KENYAEMR_CASHIER_NAMESPACE + "/api")
public class CashierRestController extends BaseRestController {
    @RequestMapping(method = RequestMethod.POST, path = "/billable-service")
    @ResponseBody
    public Object get(@RequestBody BillableServiceMapper request) {
        BillableService billableService = request.billableServiceMapper(request);
        Context.getService(IBillableItemsService.class).save(billableService);
        return true;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/upload-file")
    @ResponseBody
    public ImportResult upload(@RequestParam(value = "file") MultipartFile file, HttpServletRequest request) {
        boolean authenticated = Context.isAuthenticated();
        if (!authenticated) {
            ImportResult importResult = new ImportResult();
            importResult.setSuccess(false);
            importResult.setErrors(new ArrayList<String>());
            importResult.getErrors().add("Auth error");
            return importResult;
        }

        if (file == null) {
            ImportResult importResult = new ImportResult();
            importResult.setSuccess(false);
            importResult.setErrors(new ArrayList<String>());
            importResult.getErrors().add("null file error");
            return importResult;
        }

//        if (file.getSize() > (GlobalProperties.getStockItemsMaxUploadSize() * 1024 * 1024)) {
//            ImportResult importResult = new ImportResult();
//            importResult.setSuccess(false);
//            importResult.setErrors(new ArrayList<>());
//            importResult.getErrors().add(
//                    String.format(
//                            Context.getMessageSourceService().getMessage("stockmanagement.importoperation.maxfilesizeexceeded"),
//                            GlobalProperties.getStockItemsMaxUploadSize()));
//            return importResult;
//        }

        String contentType = file.getContentType();
        if(contentType == null || !"text/csv".equals(contentType.toLowerCase())){
            ImportResult importResult = new ImportResult();
            importResult.setSuccess(false);
            importResult.setErrors(new ArrayList<String>());
            importResult.getErrors().add("content type error");
            return importResult;
        }

        File workingDir = FileUtil.getWorkingDirectory();
        String fileName = Context.getAuthenticatedUser().getUserId().toString() + "_" + UUID.randomUUID().toString();
        File filePath = new File(workingDir, fileName);
        try

        {
            file.transferTo(filePath);
        } catch (
                Exception exception
        )

        {
            ImportResult importResult = new ImportResult();
            importResult.setSuccess(false);
            importResult.setErrors(new ArrayList<String>());
            importResult.getErrors().add("transferworkingdirfailed error");
            return importResult;
        }

        String hasHeaderParam = request.getParameter("hasHeader");
        boolean hasHeader = hasHeaderParam != null && (hasHeaderParam.toLowerCase().equals("true") || hasHeaderParam.toLowerCase().equals("1"));
        IBillableItemsService service = Context.getService(IBillableItemsService.class);
        ImportResult importResult = service.importStockItems(filePath.toPath(), hasHeader);
        importResult.setUploadSessionId(fileName);
        return importResult;
    }
}
