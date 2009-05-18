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

package com.subakva.formicid;

import com.subakva.formicid.options.OptionHandler;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class TaskFunction extends BaseFunction {

	private String taskType;
	private Container container;

	public TaskFunction(Container container, String taskType) {
		this.container = container;
		this.taskType = taskType;
   }

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		Project project = container.getProject();
		Task task = project.createTask(this.taskType);
		if (args.length == 1) {
			Scriptable options = (Scriptable) args[0];
			Object[] ids = options.getIds();
			for (int i = 0; i < ids.length; i++) {
				String id = (String) ids[i];
				Object value = options.get(id, options);

				OptionHandler handler = container.getHandler(task.getClass(), id);
				handler.handleOption(task, id, value);
			}
			task.perform();
		} else {
			//TODO: do something
			project.log(task, "Expected one argument for task: " + taskType, Project.MSG_WARN);
		}
		return Undefined.instance;
	}

	@Override
	public String getFunctionName() {
		return this.taskType;
	}
}
