package org.openmrs.module.kenyaemr.cashier.api.search;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.kenyaemr.cashier.api.base.entity.search.BaseMetadataTemplateSearch;
import org.openmrs.module.kenyaemr.cashier.api.model.PaymentMode;

public class PaymentModeSearch extends BaseMetadataTemplateSearch<PaymentMode> {

    public PaymentModeSearch(PaymentMode template) {
        super(template);
    }

    public PaymentModeSearch(PaymentMode template, Boolean includeVoided) {
        super(template, includeVoided);
    }

    @Override
    public void updateCriteria(Criteria criteria) {
        super.updateCriteria(criteria);

        PaymentMode paymentMode = getTemplate();
        if (paymentMode.getName() != null) {
            criteria.add(Restrictions.eq("name", paymentMode.getName()));
        }
    }
}
