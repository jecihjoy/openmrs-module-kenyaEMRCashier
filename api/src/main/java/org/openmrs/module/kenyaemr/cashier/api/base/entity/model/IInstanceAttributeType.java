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
package org.openmrs.module.kenyaemr.cashier.api.base.entity.model;

// @formatter:off
/**
 * Represents classes that define the attribute type information tied to a specific instance type.
 * @param <TOwner> The parent {@link IInstanceType} class.
 */
public interface 	IInstanceAttributeType<TOwner extends IInstanceType<?>>
		extends IAttributeType {
// @formatter:on
	TOwner getOwner();

	void setOwner(TOwner owner);
}
