![CI](https://api.travis-ci.org/seykron/htmlunit-maven-plugin.png "Travis CI - Continuous Integration")

htmlunit-maven-plugin
=====================

Maven plugin to load test files into HtmlUnit. This plugin is a generalization
of several projects which use HtmlUnit to run JavaScript tests over different
frameworks. The main goal is to provide the minimal infrastructure required to
dynamically build HTML files often known as "runners", which are responsible for
loading bootstrap scripts, sources and test files.

The main motivation to create this project refer to the fact that existing
plugins to run headless JavaScript tests do not fit for big, modular
projects. Some common limitations are they don't let to load resources from
classpath, they don't generate a single runner file for each test (so it's
impossible to isolate tests environment), they have a very complex codebase and
often if they provide a debug mode, it isn't very friendly.

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
* Configurable system properties.

## Requirements
* Maven 2 or 3

## Quick start
1. Add the plugin to your POM's ```build``` section. Current version available
in maven central repository is [**1.1**](CHANGELOG.md).

```
<plugin>
  <groupId>org.htmlunit</groupId>
  <artifactId>htmlunit-maven-plugin</artifactId>
  <version>1.1</version>
  <configuration>
    <runnerConfiguration>
      <outputDirectory>${project.build.directory}/htmlunit-tests</outputDirectory>
      <sourceScripts>
        file:${basedir}/src/main/resources/**/*.js;
      </sourceScripts>
      <testFiles>
        file:${basedir}/src/test/resources/**/*Test.js
      </testFiles>
    </runnerConfiguration>
  </configuration>
</plugin>
```

2. Run tests

```
mvn htmlunit:run
```

## Debugging tests
This plugin has an embedded web server to run tests from a real web browser. It
can be used setting the surefire's debug flag to true:

```
mvn htmlunit:run -Dmaven.surefire.debug=true
```

By default it starts on port 8000, but it can be configured setting the runner's
```debugPort``` attribute. The following endpoint will serve a single test:

```
http://localhost:8000/test/org/htmlunit/JasmineTest.js
```

The ```/test/``` endpoint follows the same convention as ```-Dtest``` parameter.
It is possible to specify either the fully-qualified name or the single name.


## How it works
This plugin generates an HTML test runner from a *template* for each matching
test file. Test files are loaded into the template using *runners*.

Runners are components responsible for loading tests into the template. There
exist two built-in runners: ```org.htmlunit.maven.runner.JavaScriptTestRunner```
and ```org.htmlunit.maven.runner.HtmlTestRunner```. Default runner is
```org.htmlunit.maven.runner.JavaScriptTestRunner```, and it loads test files
into the template as ```script``` tags.

The template supports several built-in placeholders which will be replaced by
configuration entries when it is processed. Supported placeholders are
```$bootstrapScripts$```, ```$sourceScripts$```, ```$testFiles$``` and
```$testRunnerScript$```. They consist of valid ant patterns and can be
specified in the runner configuration. These patterns are expanded to physical
resources when the template is processed.

Though these placeholders can be used anywhere in the template, they have a
default semantic. ```$bootstrapScripts$``` is intended to load environment
libraries required by sources and tests. ```$sourceScripts$``` are the source
files to be tested; ```$testFiles$``` are the set of tests to run and
```$testRunnerScript$``` is a single JavaScript file to prepare the environment
before running tests.

An important note: contrary to ```$bootstrapScripts$``` and
```$sourceScripts$``` which are always completely expanded and written to the
template as ```script``` tags, ```$testFiles$``` are expanded and each resource
runs in a different context. Each expanded test will have a single HTML file
written to the specified ```outputDirectory```, so they may be executed just
opening them with a browser.

That said, it can be configured as maven plugin. Default phase is "test".
Default runner template is ```org/htmlunit/maven/DefaultTestRunner.html```.


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

The following example uses [Jasmine](http://pivotal.github.io/jasmine/) to run
JavaScript tests (it's used in the plugin integration test):

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
      classpath:/META-INF/resources/webjars/jasmine-reporters/**/*_reporter.js;
      classpath:/META-INF/resources/webjars/jquery/**/*.min.js;
      file:${basedir}/src/test/resources/**/Bootstrap.js
    </bootstrapScripts>
    <sourceScripts>
      file:${basedir}/src/main/resources/**/*.js;
    </sourceScripts>
    <testFiles>
      file:${basedir}/src/test/resources/**/*Test.js
    </testFiles>
    <systemProperties>
      <customProperty>foo</customProperty>
    </systemProperties>
  </runnerConfiguration>
</configuration>
```

## Logging
This plugin uses Logback over SLF4J for logging. Logback configuration file is
not embedded into the plugin, it must be configured as a system property using
logback's property.

```
<configuration>
  <systemProperties>
    <logback.configurationFile>${basedir}/src/test/resources/logback.xml</logback.configurationFile>
  </systemProperties>
</configuration>
```

If logback is already configured in the running environment, configuration will
be inherited from context.

## Webjars
To take advantage of the flexible resource system I strongly recommend to import
JavaScript libraries as maven dependencies, so it will be easy to upgrade
libraries. Resources can be loaded using classpath patterns.
[Webjars](http://www.webjars.org/) project hosts several versions of most common
JavaScript libraries. If you don't find your required library it's possible to
make a new request to Webjars in order to upload a new lib.

The plugin integration test consist of a simple
[Jasmine](http://pivotal.github.io/jasmine/) test, and all required libraries
are loaded from Webjars. You already have noticed this piece of POM:

```
...
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>jasmine</artifactId>
  <version>1.3.1</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>jasmine-jquery</artifactId>
  <version>1.4.2</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>jquery</artifactId>
  <version>1.6.2</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.webjars</groupId>
  <artifactId>jasmine-reporters</artifactId>
  <version>0.2.1</version>
  <scope>test</scope>
</dependency>
...
```

These dependencies load Jasmine, jQuery and some utilities for jasmine into the
test classpath. Once added as dependencies, they can be loaded into the context
via classpath.

```
...
<configuration>
  <runnerConfiguration>
    ...
    <bootstrapScripts>
      classpath:/META-INF/resources/webjars/jasmine/**/*.js;
      classpath:/META-INF/resources/webjars/jasmine-reporters/**/*_reporter.js;
      classpath:/META-INF/resources/webjars/jquery/**/*.min.js;
    </bootstrapScripts>
    ...
  </runnerConfiguration>
</configuration>
```

## Development
If you'd like to contribute or to embed it into your platform, please take a
look at the [Development](Development.md) page.

Feature requests and bug fixes are welcome.

## License
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

