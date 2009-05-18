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

import com.subakva.formicid.converters.FileSetConverter;
import com.subakva.formicid.converters.ManifestConverter;
import com.subakva.formicid.converters.PathConverter;
import com.subakva.formicid.options.CopyFileSetHandler;
import com.subakva.formicid.options.JarManifestHandler;
import com.subakva.formicid.options.OptionHandler;
import com.subakva.formicid.options.ParameterHandler;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

public class Container {

	private final Scriptable scope;
	private int logLevel = Project.MSG_INFO;
	private ParameterHandler defaultHandler;
	private MultiKeyMap optionHandlers = new MultiKeyMap();

	public Container(Scriptable scope) {
		this.scope = scope;
		this.defaultHandler = new ParameterHandler(this);

		ConvertUtils.register(new PathConverter(this), Path.class);
		ConvertUtils.register(new ManifestConverter(this), Manifest.class);
		ConvertUtils.register(new FileSetConverter(this), FileSet.class);
		registerHandler(Jar.class, "manifest", new JarManifestHandler(this));
		registerHandler(Copy.class, "fileset", new CopyFileSetHandler(this));
   }

	public Converter getConverter(Class type) {
		Converter converter = ConvertUtils.lookup(type);
		if (converter == null) {
			throw new ConversionException("Unknown conversion type: " + type);
		}
		return converter;
	}

	public Scriptable getScope() {
		return this.scope;
	}

	public Project getProject() {
		Scriptable formicid = (Scriptable) scope.get("formicid", scope);
		NativeJavaObject nativeJavaObject = (NativeJavaObject) formicid.get("project", formicid);
		Project project = (Project) nativeJavaObject.unwrap();
		return project;
	}

	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	public int getLogLevel() {
		return this.logLevel;
	}

	public void log(String string, int level) {
		if (level <= getLogLevel()) {
			System.out.println(string);
		}
	}

	public void registerHandler(Class<? extends Task> taskClass, String optionName, OptionHandler handler) {
		optionHandlers.put(taskClass, optionName, handler);
	}

	public OptionHandler getHandler(Class<? extends Task> taskClass, String optionName) {
		if (optionHandlers.containsKey(taskClass, optionName)) {
			return (OptionHandler) optionHandlers.get(taskClass, optionName);
		}
		return this.defaultHandler;
	}
}
