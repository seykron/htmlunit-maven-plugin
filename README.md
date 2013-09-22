htmlunit-maven-plugin
=====================

Maven plugin to load test files into HtmlUnit. This plugin is a generalization
of several projects which use HtmlUnit to run JavaScript tests over different
frameworks. The main goal is to provide the minimal infrastructure required to
dynamically build HTML files often known as "runners", which are responsible of
loading bootstrap scripts, sources and test files.

The main motivation to create this project refer to the fact that existing
plugins to run headless JavaScript tests do not fit for big, modular
projects. Some common limitations are they don't let to load resources from
classpath, they don't generate a single runner file for each test (so it's
impossible to isolate tests environment), they have a very complex codebase and
often if they provide a debug mode, it isn't very friendly.

This project is designed under the single responsibility principle, so though
it's opened for extension it's also opened for modifications. To minimize
breaking changes in the future, it has three implementations over the core
infrastructure. For further information, look at development information below.

Finally, though it's distributed as a maven plugin, it can be used out of the
box to integrate HtmlUnit and unit/functional testing into other projects.

## Features
* Supports ant patterns to load resources from file system, network and
  classpath.
* Allows to configure how class loaders are composed.
* Provides configurable runners, including default implementations for
  JavaScript and single HTML files.
* Runner implementations are notified after each test file is processed and
  they're able to perform assertions on the DOM.
* Integrated debug mode via embedded HTTP server.

## Configuration
There're three kind of configurations: plugin, web client and runner
configurations.

Plugin configuration includes the runner class, class loaders options, timeouts
and browser version.

Web client configuration can set any value of HtmlUnit's
[WebClient](http://is.gd/1DSPrM) class such as javaScriptEnabled,
throwExceptionOnScriptError, throwExceptionOnFailingStatusCode.

Runner configuration is runner-specific configuration, though there're some
common attributes applied to all runners.

Sample configuration (it's used in the plugin integration test):

```
<configuration>
  <dependenciesClassLoader>true</dependenciesClassLoader>
  <testDependenciesClassLoader>true</testDependenciesClassLoader>
  <webClientConfiguration>
    <javaScriptEnabled>true</javaScriptEnabled>
  </webClientConfiguration>
  <runnerConfiguration>
    <outputDirectory>${project.build.directory}/htmlunit-tests</outputDirectory>
    <debugPort>9000</debugPort>
    <testRunnerTemplate>
      file:${basedir}/src/test/resources/org/htmlunit/maven/JasmineTestRunner.html
    </testRunnerTemplate>
    <bootstrapScripts>
      classpath:/META-INF/resources/webjars/jasmine/**/*.js;
      file:${basedir}/src/test/resources/**/report/*_reporter.js;
      file:${basedir}/src/test/resources/**/Bootstrap.js;
      classpath:/META-INF/resources/webjars/jquery/**/*.min.js;
    </bootstrapScripts>
    <sourceScripts>
      file:${basedir}/src/main/resources/**/*.js;
    </sourceScripts>
    <testFiles>
      file:${basedir}/src/test/resources/**/*Test.js
    </testFiles>
  </runnerConfiguration>
</configuration>
```

## Lifecycle
The plugin lifecycle consist of two phases:

* Initialization
* Execution

### Initialization
During the initialization phase, it applies default configuration to
[WebClient](http://is.gd/1DSPrM) and runners. This phase also allows
implementations to modify these entities via
```AbstractRunner#configureRunner(RunnerContext context)``` and
```AbstractRunner#configureWebClient(WebClient client)```.

### Execution
This phase executes tests once at a time and let implementations to load
tests files via
```AbstractRunner#loadTest(StringTemplate runnerTemplate, URL test)```. It uses
a simple Antlr template to perform placeholders replacement. Runners must set
the ```testFiles``` placeholder to the test content, usually ```<script>``` tags
or processed mark-up.

After placeholders replacement it writes the runner file to the configured
```outputDirectory``` and loads it into the current WebClient.

The execution phase of each test ends when the runner invokes the standard
```window.close()``` method or because a timeout. When it ends up,
```AbstractRunner#testFinished(URL test, HtmlPage page)``` is invoked to let
implementations make assertions on page's DOM.

For further information look at ```HtmlTestRunnerTest``` and
```JavaScriptTestRunnerTest``` classes.

## Extension: how to create new runners
This plugin can be extended creating new runners. Runners are classes
responsible of loading test content into the runner template. It must extend
```AbstractRunner``` as it's shown in the following example:

```
package com.my.project;

/** Runner to load tests from plain HTML files.
 */
public class CustomTestRunner extends AbstractRunner {

  /** Loads test content from somewhere.
   * {@inheritDoc}
   */
  protected void loadTest(final StringTemplate runnerTemplate,
      final URL test) {
    String testContent = loadTestContentFromSomewhere(test);
    runnerTemplate.setAttribute("testFiles", testContent);
  }
}
```
Once created, the runner can be specified in the plugin POM's configuration:

```
<configuration>
  <runnerClassName>com.my.project.CustomTestRunner</runnerClassName>
</configuration>
```

### Development
New features are welcome but must follow these constraints:

* Keep it simple: this project is micro-infrastructure, it's not intended to
solve more problems than it solves right now. I will create another repository
to upload custom runners to integrate specific frameworks.

* Agree before coding: send a message, or an email, or whatever you feel
comfortable but please ask before coding. Some features already exist or they
can be solved in another ways. This documentation is prelimitar and it lacks a
lot of important internals.

* Follow code conventions: if you look at the code you will notice it looks
similar everywhere. Code conventions are not related to beauty, they're related
to consistence. I adopted the standard Sun conventions with a little change:
two spaces instead of four for indentation.

* Be humble: I like to review the code I commit. I make my own self-review
before committing code to the remote repository, so I'll make the same with
push requests. I understand sometimes is very difficult to agree with code
changes but discussion is necessary to build a common knowledge.

### License
    Copyright 2013 seykron (https://github.com/seykron/htmlunit-maven-plugin)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

