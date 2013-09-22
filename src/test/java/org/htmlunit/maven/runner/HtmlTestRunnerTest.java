package org.htmlunit.maven.runner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.net.URL;
import java.util.Properties;

import org.htmlunit.maven.RunnerContext;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/** Tests the {@link HtmlTestRunner} class.
 */
public class HtmlTestRunnerTest {
  private RunnerContext context;
  private HtmlTestRunner runner;
  private boolean verified;

  @Before
  public void setUp() {
    context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put("outputDirectory",
        System.getProperty("java.io.tmpdir"));
    runnerConfig.put("testFiles",
        "classpath:org/htmlunit/maven/*Test.html");

    context.setTimeout(10);
    context.getWebClientConfiguration().setProperty("javaScriptEnabled",
        String.valueOf(true));
    context.setRunnerConfiguration(runnerConfig);
  }

  @Test
  public void run() {
    runner = new HtmlTestRunner() {
      @Override
      protected void testFinished(final URL test, final HtmlPage page) {
        String result = page.getElementById("main").asText();

        if (test.getFile().endsWith("FirstTest.html")) {
          assertThat(result, is("Joe"));
        } else if (test.getFile().endsWith("SecondTest.html")) {
          assertThat(result, is("Moe"));
        }
        verified = true;
      }
    };
    runner.initialize(context);
    runner.run();
    assertThat(verified, is(true));
  }
}
