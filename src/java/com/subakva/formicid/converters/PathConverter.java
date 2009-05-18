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

import com.subakva.formicid.Container;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Path;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class PathConverter implements Converter {

	private final Container container;

	public PathConverter(Container container) {
		this.container = container;
	}

	public Path convert(Class type, Object value) {
		if (value == null || value.equals(Undefined.instance)) {
			throw new ConversionException("No value specified");
		}
		if (value instanceof Path) {
			return (Path) value;
		} else if (value instanceof Scriptable) {
			Path path = createNewPath();
			Scriptable js = ((Scriptable) value);
			Object[] ids = js.getIds();
			for (int i = 0; i < ids.length; i++) {
				String id = (String) ids[i];
				Object sub = js.get(id, js);
				path.add(this.convert(type, sub));
			}
			return path;
		} else if (value instanceof String) {
			Path path = createNewPath();
			FileList fileList = new FileList();
			fileList.setProject(getProject());
			fileList.setFiles((String) value);
			path.addFilelist(fileList);
			return path;
		} else {
			throw new ConversionException("" + value);
		}
	}

	private Project getProject() {
		return this.container.getProject();
	}

	protected Path createNewPath() {
		return (Path) getProject().createDataType("path");
	}
}
