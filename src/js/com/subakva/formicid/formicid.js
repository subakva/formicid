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

var out = Packages.java.lang.System.out;

/**
 * Initializes the build system by creating a Project using the provided targets. This function must
 * be called in the build script to initialize the system.
 *
 * @param Object targets - a JS object containing the target definitions
 * @param String defaultTarget - the name of the target to execute if none is specified
 */
formicid.init = function(targets, defaultTarget) {
	var p = new formicid.Project(targets, defaultTarget);
	this.project = p._project;
}

/**
 * Executes the specified build command using the specified target names. This function is invoked
 * automatically when the build is invoked from the command-line.
 *
 * Valid commands are 'build' and 'projecthelp'.
 *
 * @param String command - the name of the formicid function to invoke
 * @param Array targets - an array of target names to invoke
 */
formicid.start = function(command, targets) {
	this[command].call(this, targets);
}

/**
 * Executes the specified build targets and any of their dependencies.
 *
 * @param Array targets - an array of target names to invoke
 */
formicid.build = function(targets) {
	this.project.fireBuildStarted();
	if (targets.length == 0) {
		this.project.executeTarget(this.project.getDefaultTarget());
		this.project.fireBuildFinished(null);
	} else {
		for (var name in targets) {
			var targetName = targets[name];
			try {
				this.project.executeTarget(targetName);
				this.project.fireBuildFinished(null);
			} catch(e) {
				this.project.fireBuildFinished(e.javaException);
			}
		}
	}
}

/**
 * Prints out the list of build targets that have descriptions.
 */
formicid.projecthelp = function() {
	out.println('Would you believe that ant Main.printTargets() is private!?');
}

formicid.Project = function(targets, defaultTarget) {
	this.targets = targets;
	this._project = this.initProject(targets, defaultTarget);
}
formicid.Project.prototype = {
	initProject: function(targets, defaultTarget) {
		var project = new Packages.org.apache.tools.ant.Project();
		project.setName('formicid');
		project.setBasedir('.');
		project.setDefault(defaultTarget);
		project.addBuildListener(this.initLogger());
		project.addBuildListener(this.initRunner());
		project.init();

		for (var name in targets) {
			var def = targets[name];
			var target = new formicid.Target(name, def);
			project.addTarget(target._target);
		}

		return project;
	},
	initLogger: function() {
		var logger = new Packages.org.apache.tools.ant.DefaultLogger();
		logger.setMessageOutputLevel(formicid.logLevel);
		logger.setOutputPrintStream(Packages.java.lang.System.out);
		logger.setErrorPrintStream(Packages.java.lang.System.err);
		return logger;
	},
	initRunner: function() {
		var self = this;
		var targetRunnerImpl = {
			buildStarted: function(event) {},
			buildFinished: function(event) {},
			targetStarted: function(event) {
				//execute the javascript target
				var name = event.getTarget().getName();
				var target = self.targets[name];
				target.run.call(formicid.global);
			},
			targetFinished: function(event) {},
			taskStarted: function(event) {},
			taskFinished: function(event) {},
			messageLogged: function(event) {},
		}
		return new Packages.org.apache.tools.ant.BuildListener(targetRunnerImpl);
	},
	run: function(targetName) {
		out.println('Running target: ' + targetName);
		if (targetName) {
			this._project.executeTarget(targetName);
		} else {
			this._project.executeTarget(this._project.getDefaultTarget());
		}
	}
}

formicid.Target = function(name, def) {
	this._target = new Packages.org.apache.tools.ant.Target();
	this._target.setName(name);
	if (def.description) { this._target.setDescription(def.description); }
	if (def.depends) { this._target.setDepends(def.depends + ''); }
}
