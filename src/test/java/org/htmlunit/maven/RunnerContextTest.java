package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/** Tests the {@link RunnerContext} class.
 */
public class RunnerContextTest {

  @Test
  public void construct() {
    RunnerContext context = new RunnerContext();
    assertThat(context.getBrowserVersion(), is(BrowserVersion.FIREFOX_17));
    assertThat(context.getWebClientConfiguration(), is(notNullValue()));
    assertThat(context.getRunnerConfiguration(), is(notNullValue()));
    assertThat(context.getTimeout(), is(-1));
    assertThat(context.getLog(), is(nullValue()));
  }

  @Test
  public void configure_default() throws Exception {
    RunnerContext context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put("outputDirectory", System.getProperty("java.io.tmpdir"));

    context.setRunnerConfiguration(runnerConfig);
    context.init();

    assertThat(context.getBrowserVersion(),
        is(BrowserVersion.FIREFOX_17));
    assertThat(context.getWebClientConfiguration(), is(notNullValue()));
    assertThat(context.getRunnerConfiguration(), is(runnerConfig));
    assertThat(context.getTimeout(), is(-1));
    assertThat(context.getLog(), is(nullValue()));
    assertThat(context.isDebugMode(), is(false));
    assertThat(context.getDebugPort(), is(8000));
    assertThat(context.isJavaScriptEnabled(), is(false));
    assertThat(context.getTestRunnerTemplate(),
        is(new URL(RunnerContext.DEFAULT_TEMPLATE)));
    assertThat(context.getBootstrapScripts().size(), is(0));
    assertThat(context.getSourceScripts().size(), is(0));
    assertThat(context.getTestRunnerScript(), is(nullValue()));
    assertThat(context.getTestFiles().size(), is(0));
    assertThat(context.getOutputDirectory(), is(notNullValue()));
  }

  @Test(expected = RuntimeException.class)
  public void configure_missingOutputDir() throws Exception {
    RunnerContext context = new RunnerContext();
    context.init();
  }

  @Test
  public void configure_javaScriptDisabled() throws Exception {
    RunnerContext context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put("outputDirectory", System.getProperty("java.io.tmpdir"));
    runnerConfig.put("testRunnerScript",
        "classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put("bootstrapScripts",
        "classpath:org/htmlunit/maven/Bootstrap.js;"
        + "http://code.jquery.com/jquery-1.9.1.js");
    runnerConfig.put("sourceScripts",
        "classpath:org/htmlunit/maven/*.js;"
        + "~classpath:org/htmlunit/maven/*Test.js;"
        + "~classpath:org/htmlunit/maven/Bootstrap.js;"
        + "~classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put("testFiles",
        "classpath:org/htmlunit/maven/*Test.js");
    runnerConfig.put("debugPort", "9000");

    Properties webClientConfig = new Properties();
    Log log = new SystemStreamLog();

    context.setBrowserVersion(BrowserVersion.INTERNET_EXPLORER_8);
    context.setRunnerConfiguration(runnerConfig);
    context.setWebClientConfiguration(webClientConfig);
    context.setTimeout(60);
    context.setLog(log);
    context.setDebugMode(true);
    context.init();

    assertThat(context.getBrowserVersion(),
        is(BrowserVersion.INTERNET_EXPLORER_8));
    assertThat(context.getWebClientConfiguration(), is(webClientConfig));
    assertThat(context.getRunnerConfiguration(), is(runnerConfig));
    assertThat(context.getTimeout(), is(60));
    assertThat(context.getLog(), is(log));
    assertThat(context.isDebugMode(), is(true));
    assertThat(context.getDebugPort(), is(9000));
    assertThat(context.isJavaScriptEnabled(), is(false));
    assertThat(context.getTestRunnerTemplate(),
        is(new URL(RunnerContext.DEFAULT_TEMPLATE)));
    assertThat(context.getBootstrapScripts().size(), is(0));
    assertThat(context.getSourceScripts().size(), is(0));
    assertThat(context.getTestRunnerScript(), is(nullValue()));
    assertThat(context.getTestFiles().size(), is(0));
    assertThat(context.getOutputDirectory(), is(notNullValue()));
  }

  @Test
  public void configure_javaScriptEnabled() throws Exception {
    RunnerContext context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put("outputDirectory", System.getProperty("java.io.tmpdir"));
    runnerConfig.put("testRunnerScript",
        "classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put("bootstrapScripts",
        "classpath:org/htmlunit/maven/Bootstrap.js;"
        + "http://code.jquery.com/jquery-1.9.1.js");
    runnerConfig.put("sourceScripts",
        "classpath:org/htmlunit/maven/*.js;"
        + "~classpath:org/htmlunit/maven/*Test.js;"
        + "~classpath:org/htmlunit/maven/Bootstrap.js;"
        + "~classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put("testFiles",
        "classpath:org/htmlunit/maven/*Test.js");
    runnerConfig.put("testRunnerTemplate", RunnerContext.DEFAULT_TEMPLATE
        + "foo");

    Properties webClientConfig = new Properties();
    webClientConfig.setProperty("javaScriptEnabled", "true");
    Log log = new SystemStreamLog();

    context.setBrowserVersion(BrowserVersion.INTERNET_EXPLORER_8);
    context.setWebClientConfiguration(webClientConfig);
    context.setRunnerConfiguration(runnerConfig);
    context.setTimeout(60);
    context.setLog(log);
    context.setDebugMode(true);
    context.init();

    assertThat(context.getBrowserVersion(),
        is(BrowserVersion.INTERNET_EXPLORER_8));
    assertThat(context.getWebClientConfiguration(), is(webClientConfig));
    assertThat(context.getRunnerConfiguration(), is(runnerConfig));
    assertThat(context.getTimeout(), is(60));
    assertThat(context.getLog(), is(log));
    assertThat(context.isDebugMode(), is(true));
    assertThat(context.getDebugPort(), is(8000));
    assertThat(context.isJavaScriptEnabled(), is(true));
    assertThat(context.getTestRunnerTemplate(),
        is(new URL(RunnerContext.DEFAULT_TEMPLATE + "foo")));
    assertThat(context.getTestRunnerScript().toString()
        .endsWith("org/htmlunit/maven/TestRunner.js"), is(true));
    assertThat(context.getBootstrapScripts().size(), is(2));
    assertThat(context.getBootstrapScripts().get(0).toString()
        .endsWith("org/htmlunit/maven/Bootstrap.js"), is(true));
    assertThat(context.getSourceScripts().size(), is(2));
    assertThat(context.getSourceScripts().get(0).toString()
        .endsWith("Widget.js"), is(true));
    assertThat(context.getTestFiles().size(), is(3));
    assertThat(context.getTestFiles().get(0).toString()
        .endsWith("WidgetTest.js"), is(true));
    assertThat(context.getOutputDirectory(),
        is(new File(System.getProperty("java.io.tmpdir"))));
  }
}
