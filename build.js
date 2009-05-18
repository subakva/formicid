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

/* Directories used in the build process
**********************************************************/
var dir = {};
dir.lib = 'lib';
dir.antlib = dir.lib + '/ant/lib';
dir.src = 'src';
dir.build = 'build';
dir.classes = dir.build + '/classes';
dir.temp = dir.build + '/temp';

/* Paths (including classpaths) used in the build process.
**********************************************************/
var path = {};
path.compile = {
	js: dir.lib + '/js.jar',
	cli: dir.lib + '/commons-cli.jar',
	logging: dir.lib + '/commons-logging.jar',
	cli: dir.lib + '/commons-cli.jar',
	beanutils: dir.lib + '/commons-beanutils.jar',
	collections: dir.lib + '/commons-collections.jar'
}
path.run = {
	ant: dir.antlib + '/ant.jar',
	launcher: dir.antlib + '/ant-launcher.jar',
	logging: dir.lib + '/commons-logging.jar',
	classes: dir.classes,
	compile: path.compile
}

/* Targets
**********************************************************/
var target = {};
target.init = { description: '--> Creates transient directories.',
	run: function() {
		mkdir({dir: dir.build});
		mkdir({dir: dir.classes});
	}
}
target.clean = { description: '--> Removes all build artifacts.',
	run: function() {
		rm({ dir: dir.build });
	}
}
target.compile = { description: '--> Compiles java classes.', depends: ['init'],
	run: function() {
		javac({ srcDir: dir.src, destDir: dir.classes, classpath: path.compile, debug: true });
		copy({
			todir: dir.classes,
			fileset: { dir: dir.src, includes: '**/*.js' }
		});
	}
}
target.jar = { description: '--> Creates executable formicid.jar.', depends: ['compile'],
	run: function() {
		mkdir({dir: dir.temp});

		unjar({ src: path.compile.js, dest: dir.temp, overwrite: false });
		unjar({ src: path.compile.cli, dest: dir.temp, overwrite: false });
		unjar({ src: path.compile.beanutils, dest: dir.temp, overwrite: false });
		unjar({ src: path.compile.collections, dest: dir.temp, overwrite: false });
		unjar({ src: path.run.ant, dest: dir.temp, overwrite: false });
		unjar({ src: path.run.launcher, dest: dir.temp, overwrite: false });
		unjar({ src: path.run.logging, dest: dir.temp, overwrite: false });

		copy({ file: 'LICENSE', todir: dir.temp });
		copy({ todir: dir.temp,
			fileset: { dir: dir.classes, includes: '**/*' }
		});

		jar({
			baseDir: dir.temp, destFile:dir.build + '/formicid.jar',
			manifest: { 'Main-Class': 'com.subakva.formicid.Main' }
		});
	}
}

/* Initialize the system by telling it where our targets are defined.
**********************************************************/
formicid.init(target, 'compile');
