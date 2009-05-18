REM /*
REM  * Copyright 2007 Jason Wadsworth
REM  * 
REM  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
REM  * use this file except in compliance with the License. You may obtain a copy of
REM  * the License at
REM  * 
REM  * http://www.apache.org/licenses/LICENSE-2.0
REM  * 
REM  * Unless required by applicable law or agreed to in writing, software
REM  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
REM  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
REM  * License for the specific language governing permissions and limitations under
REM  * the License.
REM  */

set FORMICID_CP=build/classes/
set FORMICID_CP=%FORMICID_CP%;lib/js.jar
set FORMICID_CP=%FORMICID_CP%;lib/ant/lib/ant.jar
set FORMICID_CP=%FORMICID_CP%;lib/ant/lib/ant-launcher.jar
set FORMICID_CP=%FORMICID_CP%;lib/commons-collections.jar
set FORMICID_CP=%FORMICID_CP%;lib/commons-beanutils.jar
set FORMICID_CP=%FORMICID_CP%;lib/commons-cli.jar
set FORMICID_CP=%FORMICID_CP%;lib/commons-logging.jar

java -cp %FORMICID_CP% com.subakva.formicid.Main %*

