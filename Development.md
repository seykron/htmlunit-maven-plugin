Development
===========

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

### Collaboration
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

