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

import com.subakva.formicid.Container;

import org.apache.commons.beanutils.Converter;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;

public class JarManifestHandler implements OptionHandler {
	
	private final Container container;

	public JarManifestHandler(Container container) {
		this.container = container;
	}

	public void handleOption(Task task, String optionName, Object value) {
		Converter converter = this.container.getConverter(Manifest.class);
		Manifest manifest = (Manifest) converter.convert(Manifest.class, value);
		if (task instanceof Jar) {
			try {
				Jar jar = ((Jar) task);
				jar.addConfiguredManifest(manifest);
			} catch (ManifestException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Invalid task type: " + task);
		}
	}
}
