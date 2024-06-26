/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemr.cashier.rest.controller.base;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainSubResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * REST sub controller for Cashier resources.
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + CashierSubResourceController.KENYAEMR_CASHIER_NAMESPACE)
public class CashierSubResourceController extends MainSubResourceController {
	public static final String KENYAEMR_CASHIER_NAMESPACE = "/cashier";
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + CashierSubResourceController.KENYAEMR_CASHIER_NAMESPACE;
	}
}
