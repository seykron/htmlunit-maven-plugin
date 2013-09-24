package org.htmlunit.maven;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.Properties;

import org.antlr.stringtemplate.StringTemplate;
import org.htmlunit.javascript.EventHandler;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.AbstractRunner.RunnerDriver;
import org.htmlunit.maven.RunnerContext;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

/** Tests the {@link AbstractRunner} class.
 * TODO (matias.mirabelli): test connection and web window listeners.
 */
public class AbstractRunnerTest {

  @Test
  public void configuration() {
    final RunnerContext context = createMock(RunnerContext.class);
    AbstractRunner runner = new AbstractRunner() {
      @Override
      public void run() {
      }
      @Override
      protected void loadTest(final StringTemplate runnerTemplate,
          final URL test) {
      }
    };
    expect(context.getBrowserVersion()).andReturn(BrowserVersion.FIREFOX_17);

    Properties clientProps = new Properties();
    clientProps.put("homePage", "http://foo.bar");
    expect(context.getWebClientConfiguration()).andReturn(clientProps);
    expect(context.getTimeout()).andReturn(60);
    context.init();
    replay(context);

    assertThat(runner.getContext(), is(nullValue()));
    assertThat(runner.getDriver(), is(nullValue()));

    runner.initialize(context);

    WebClient client = ((RunnerDriver) runner.getDriver()).getWebClient();
    assertThat(client.getOptions().getHomePage(), is("http://foo.bar"));
    assertThat(client.getOptions().isJavaScriptEnabled(), is(true));
    assertThat(client.getAjaxController(),
        instanceOf(NicelyResynchronizingAjaxController.class));
    assertThat(runner.getContext(), is(context));
    assertThat(runner.getDriver(), is(notNullValue()));
    verify(context);
  }

  @SuppressWarnings("serial")
  @Test
  public void addEventListener() {
    AbstractRunner runner = new AbstractRunner() {
      @Override
      public void run() {
        getDriver().get("classpath:org/htmlunit/maven/TestRunner.js");
      }
      @Override
      protected void loadTest(final StringTemplate runnerTemplate,
          final URL test) {
      }
    };
    RunnerContext context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put("outputDirectory", System.getProperty("java.io.tmpdir"));
    context.setRunnerConfiguration(runnerConfig);

    runner.initialize(context);
    runner.addEventListener(Event.TYPE_DOM_DOCUMENT_LOADED, new EventHandler() {
      @Override
      public void handleEvent(final Event event) {
        assertThat(event.getCurrentTarget(), is(notNullValue()));
        assertThat(event.getCurrentTarget(), is(notNullValue()));
        assertThat(event.getCurrentTarget(), instanceOf(Window.class));
      }
    }, false);
    runner.run();
  }
}
