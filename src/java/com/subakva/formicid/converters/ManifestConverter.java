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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.subakva.formicid.Container;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class ManifestConverter implements Converter {

	private final Container container;

	public ManifestConverter(Container container) {
		this.container = container;
	}

	protected Project getProject() {
		return container.getProject();
	}

	public Manifest convert(Class type, Object value) {
		if (value == null || value.equals(Undefined.instance)) {
			throw new ConversionException("No value specified");
		}
		if (value instanceof Manifest) {
			return (Manifest) value;
		} else if (value instanceof String) {
			try {
				Manifest manifest = new Manifest(new FileReader(new File((String) value)));
				return manifest;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (ManifestException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (value instanceof Scriptable) {
			Manifest manifest = new Manifest();
			Scriptable props = (Scriptable) value;
			Object[] ids = props.getIds();
			for (int i = 0; i < ids.length; i++) {
				try {
					String attrValue = (String) ids[i];
					String attrName = (String) props.get(attrValue, props);
					Manifest.Attribute attr = new Manifest.Attribute(attrValue, attrName);
					manifest.addConfiguredAttribute(attr);
				} catch (ManifestException e) {
					throw new RuntimeException(e);
				}
			}
			return manifest;
		} else {
			throw new ConversionException("" + value);
		}
	}
}
