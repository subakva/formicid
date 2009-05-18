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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Main {

	private static final String CLI_SYNTAX = "java -jar formicid.jar [options] [target1 target2 ...]";

	public static void main(final String[] args) {
		Options options = new Options();
		options.addOption("p", "projecthelp", false, "print project help information");
		options.addOption("f", "file", true, "use given buildfile");
		options.addOption("h", "help", false, "print this message");
		options.addOption("d", "debug", false, "print debugging information");
		options.addOption("v", "verbose", false, "be extra verbose");
		options.addOption("q", "quiet", false, "be extra quiet");

		CommandLine cli;
		try {
			cli = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			System.out.println("Error: " + e.getMessage());
			new HelpFormatter().printHelp(CLI_SYNTAX, options);
			return;
		}

		String scriptName = cli.getOptionValue("f", "build.js");
		if (cli.hasOption("h")) {
			new HelpFormatter().printHelp(CLI_SYNTAX, options);
			return;
		} else if (cli.hasOption("p")) {
			runScript(scriptName, "projecthelp", null);
		} else {
			runScript(scriptName, "build", cli);
		}
	}

	protected static void runScript(final String scriptName, final String command,
			final CommandLine cli) {
		Context.call(new ContextAction() {
			public Object run(Context cx) {
				try {
					ScriptableObject global = cx.initStandardObjects();
					Container container = new Container(global);

					NativeObject formicid = new NativeObject();
					container.setLogLevel(getLogLevel(cli));

					formicid.put("global", formicid, global);
					formicid.put("logLevel", formicid, getLogLevel(cli));

					global.put("formicid", global, formicid);
					evaluateResource(cx, global, "formicid.js");

					defineTasks(container);

					cx.evaluateReader(global, new FileReader(scriptName), scriptName, 1, null);

					String array = toArrayString(cli.getArgs());
					String script = "formicid.start('" + command + "', " + array + ");";
					cx.evaluateString(global, script, "Main.runScript", 1, null);
					return global;
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	protected static int getLogLevel(CommandLine cli) {
		if (cli.hasOption("d")) {
			return Project.MSG_DEBUG;
		} else if (cli.hasOption("v")) {
			return Project.MSG_VERBOSE;
		} else if (cli.hasOption("q")) {
			return Project.MSG_WARN;
		} else {
			return Project.MSG_INFO;
		}
	}

	private static void defineTasks(Container container) throws IOException {
		Scriptable scope = container.getScope();
		InputStream stream = Task.class
			.getResourceAsStream("/org/apache/tools/ant/taskdefs/defaults.properties");
		Properties p = new Properties();
		p.load(stream);

		for (Iterator iter = p.keySet().iterator(); iter.hasNext();) {
			String taskType = (String) iter.next();
			String functionName = taskType;
			if ("delete".equals(taskType)) {
				functionName = "rm";
			}
			scope.put(functionName, scope, new TaskFunction(container, taskType));
			//Path, Resource, Zip$WhenEmpty, Jar$FilesetManifestConfig, Zip$Duplicate,
			//ExecuteOn$FileDirBoth, Commandline, ExecuteStreamHandler, Tar$TarLongFileMode,
			//Tar$TarCompressionMethod, Javadoc$AccessType, Echo$EchoLevel, Definer$OnError,
			//Definer$Format, ClassLoader, Available$FileDir, Untar$UntarCompressionMethod,
			//WaitFor$Unit, Writer, FixCRLF$CrLf, EmailTask$Encoding, Checksum$FormatElement,
			//Reference, Recorder$VerbosityLevelChoices, Recorder$ActionChoices,
			//FixCRLF$AddAsisRemove, URL, Comparison, Date, Length$FileMode, SQLExec$OnError,
			//SQLExec$DelimiterType
		}
	}

	private static String toArrayString(String[] targets) {
		if (targets == null) {
			targets = new String[0];
		}
		StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < targets.length; i++) {
			builder.append("'");
			builder.append(targets[i]);
			builder.append("'");
			if (i < targets.length - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		String array = builder.toString();
		return array;
	}

	private static void evaluateResource(Context cx, ScriptableObject scope, String resource)
			throws IOException {
		InputStream stream = Main.class.getResourceAsStream(resource);
		if (stream != null) {
			cx.evaluateReader(scope, new InputStreamReader(stream), resource, 1, null);
			stream.close();
		} else {
			throw new RuntimeException("Unable to load resource: " + resource);
		}
	}
}
