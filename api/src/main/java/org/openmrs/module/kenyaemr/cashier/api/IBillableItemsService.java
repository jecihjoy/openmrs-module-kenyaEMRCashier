package org.openmrs.module.kenyaemr.cashier.api;

import org.openmrs.module.kenyaemr.cashier.api.base.entity.IEntityDataService;
import org.openmrs.module.kenyaemr.cashier.api.model.BillableService;
import org.openmrs.module.kenyaemr.cashier.api.search.BillableServiceSearch;
import org.openmrs.module.kenyaemr.cashier.jobs.ImportResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

@Transactional
public interface IBillableItemsService extends IEntityDataService<BillableService> {
    List<BillableService> findServices(final BillableServiceSearch search);
    ImportResult importStockItems(Path file, boolean hasHeader);
}
