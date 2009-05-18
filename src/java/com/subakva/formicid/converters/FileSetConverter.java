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

package com.subakva.formicid.converters;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import com.subakva.formicid.Container;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class FileSetConverter implements Converter {

	private final Container container;

	public FileSetConverter(Container container) {
		this.container = container;
	}

	public FileSet convert(Class type, Object value) {
		if (value == null || value.equals(Undefined.instance)) {
			throw new ConversionException("No value specified");
		}
		if (value instanceof FileSet) {
			return (FileSet) value; 
		} else if (value instanceof Scriptable) {
			FileSet fileSet = (FileSet) container.getProject().createDataType("fileset");
			Scriptable options = (Scriptable) value;
			Object[] ids = options.getIds();
			for (int i = 0; i < ids.length; i++) {
				String id = (String) ids[i];
				Object val = options.get(id, options);
				try {
					PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(fileSet, id);
					Class<?> propertyType = descriptor.getPropertyType();
					Converter converter = container.getConverter(propertyType);
					Object converted = converter.convert(propertyType, val);
					PropertyUtils.setProperty(fileSet, id, converted);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
			return fileSet;
		} else if (value instanceof String) {
			FileSet fileSet = (FileSet) container.getProject().createDataType("fileset");
			fileSet.setDir(new File("."));
			FilenameSelector selector = new FilenameSelector();
			selector.setName((String) value);
			fileSet.add(selector);
			return fileSet;
		} else {
			throw new ConversionException("" + value);
		}
	}
}
