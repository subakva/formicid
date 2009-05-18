/*
 * Copyright 2007 Jason Wadsworth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.subakva.formicid.options;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import com.subakva.formicid.Container;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class ParameterHandler implements OptionHandler {

	private static HashSet<String> excluded = new HashSet<String>();
	private final Container container;

	static {
		PropertyDescriptor[] taskProps = PropertyUtils.getPropertyDescriptors(Task.class);
		for (PropertyDescriptor descriptor : taskProps) {
			if (descriptor.getWriteMethod() != null) {
				ParameterHandler.excluded.add(descriptor.getName());
			}
		}
	}

	public ParameterHandler(Container container) {
		this.container = container;
	}

	public void handleOption(Task task, String optionName, Object value) {
		HashMap<String, PropertyDescriptor> properties = getWritableProperties(task.getClass());
		PropertyDescriptor descriptor = properties.get(optionName.toLowerCase());
		if (descriptor == null) {
			throw new RuntimeException("Unknown property for " + task.getTaskType() + " task: "
				+ optionName);
		}
		Class<?> type = descriptor.getPropertyType();
		Converter converter = container.getConverter(type);
		Object converted = converter.convert(type, value);
		try {
			task.log("converting property: " + descriptor.getName(), Project.MSG_DEBUG);
			task.log("converter: " + converter, Project.MSG_DEBUG);
			task.log("converted: " + converted, Project.MSG_DEBUG);

			descriptor.getWriteMethod().invoke(task, new Object[] {converted});
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	protected HashMap<String, PropertyDescriptor> getWritableProperties(Class taskClass) {
		HashMap<String, PropertyDescriptor> writableProperties = new HashMap<String, PropertyDescriptor>();
		PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(taskClass);
		for (PropertyDescriptor descriptor : descriptors) {
			if (descriptor.getWriteMethod() != null && !excluded.contains(descriptor.getName())) {
				writableProperties.put(descriptor.getName().toLowerCase(), descriptor);
			}
		}
		return writableProperties;
	}
}
