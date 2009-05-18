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
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

public class CopyFileSetHandler implements OptionHandler {

	private final Container container;

	public CopyFileSetHandler(Container container) {
		this.container = container;
	}

	public void handleOption(Task task, String optionName, Object value) {
		Converter converter = this.container.getConverter(FileSet.class);
		FileSet fileset = (FileSet) converter.convert(FileSet.class, value);
		if (task instanceof Copy) {
			Copy copy = ((Copy) task);
			copy.addFileset(fileset);
		} else {
			throw new RuntimeException("Invalid task type: " + task);
		}
	}
}
