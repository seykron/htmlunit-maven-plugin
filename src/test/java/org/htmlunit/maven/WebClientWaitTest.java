package org.htmlunit.maven;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

/** Tests the {@link WebClientWait} class.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WebWindowEvent.class)
public class WebClientWaitTest {

  private WebClient client;
  private WebClientOptions options;
  private Capture<JavaScriptErrorListener> capturedErrorListener;
  private Capture<WebWindowListener> capturedWebWindowListener;

  @Before
  public void setUp() {
    options = createMock(WebClientOptions.class);
    options.setThrowExceptionOnScriptError(false);
    replay(options);

    client = createMock(WebClient.class);
    expect(client.getOptions()).andReturn(options);
    capturedErrorListener = new Capture<JavaScriptErrorListener>();
    capturedWebWindowListener = new Capture<WebWindowListener>();
    client.addWebWindowListener(capture(capturedWebWindowListener));
    client.setJavaScriptErrorListener(capture(capturedErrorListener));
  }

  @Test
  public void construct() {
    WebWindow window = createMock(WebWindow.class);
    expect(window.getScriptObject()).andReturn(new Object());
    replay(window);

    expect(client.getWebWindows()).andReturn(Arrays.asList(window));
    replay(client);

    WebClientWait wait = new WebClientWait(client);
    assertThat(wait.getWebClient(), is(client));

    verify(client, options, window);
  }

  @Test
  public void events() {
    WebWindow window = createMock(WebWindow.class);
    expect(window.getScriptObject()).andReturn(new Object());
    replay(window);

    expect(client.getWebWindows()).andReturn(new ArrayList<WebWindow>());
    replay(client);

    WebClientWait wait = new WebClientWait(client);
    assertThat(wait.isDone(), is(true));

    WebWindowListener listener = capturedWebWindowListener.getValue();

    WebWindowEvent contentChangedEvent = createMock(WebWindowEvent.class);
    expect(contentChangedEvent.getWebWindow()).andReturn(window);
    replay(contentChangedEvent);
    listener.webWindowContentChanged(contentChangedEvent);
    assertThat(wait.isDone(), is(false));

    WebWindowEvent closeEvent = createMock(WebWindowEvent.class);
    expect(closeEvent.getWebWindow()).andReturn(window);
    replay(closeEvent);
    listener.webWindowClosed(closeEvent);
    assertThat(wait.isDone(), is(true));

    verify(client, options, window, contentChangedEvent, closeEvent);
  }

  @Test
  public void start() throws InterruptedException {
    WebWindow window = createMock(WebWindow.class);
    expect(window.getScriptObject()).andReturn(new Object());
    replay(window);

    expect(client.getWebWindows()).andReturn(new ArrayList<WebWindow>());
    replay(client);

    final long startTime = System.currentTimeMillis();

    WebClientWait wait = new WebClientWait(client) {
      @Override
      boolean isDone() {
        long currentTime = System.currentTimeMillis() - startTime;
        assertThat(currentTime >= 500, is(true));
        return currentTime >= 1500;
      }
    };
    wait.pollingEvery(500, TimeUnit.MILLISECONDS)
      .withTimeout(5000, TimeUnit.MILLISECONDS);
    Thread.sleep(500);
    wait.start();

    verify(client, options);
  }

  @Test(expected = TimeoutException.class)
  public void start_timeout() throws InterruptedException {
    WebWindow window = createMock(WebWindow.class);
    expect(window.getScriptObject()).andReturn(new Object());
    replay(window);

    expect(client.getWebWindows()).andReturn(new ArrayList<WebWindow>());
    replay(client);

    WebClientWait wait = new WebClientWait(client) {
      @Override
      boolean isDone() {
        return false;
      }
    };
    wait.pollingEvery(500, TimeUnit.MILLISECONDS)
      .withTimeout(1000, TimeUnit.MILLISECONDS);
    wait.start();
    verify(client, options);
  }

  @Test(expected = RuntimeException.class)
  public void start_throwJavaScriptException() {
    WebWindow window = createMock(WebWindow.class);
    expect(window.getScriptObject()).andReturn(new Object());
    replay(window);

    expect(client.getWebWindows()).andReturn(new ArrayList<WebWindow>());
    replay(client);

    WebClientWait wait = new WebClientWait(client);
    wait.setThrowJavaScriptException(true)
      .pollingEvery(500, TimeUnit.MILLISECONDS)
      .withTimeout(10000, TimeUnit.MILLISECONDS);

    JavaScriptErrorListener listener = capturedErrorListener.getValue();
    HtmlPage page = createMock(HtmlPage.class);
    ScriptException ex;
    ex = new ScriptException(page, new IllegalStateException("Test"));
    listener.scriptException(page, ex);

    wait.start();
    verify(client, options);
  }

  @Test
  public void start_quietJavaScriptException() {
    WebWindow window = createMock(WebWindow.class);
    expect(window.getScriptObject()).andReturn(new Object());
    replay(window);

    expect(client.getWebWindows()).andReturn(new ArrayList<WebWindow>());
    replay(client);

    WebClientWait wait = new WebClientWait(client);
    wait.pollingEvery(500, TimeUnit.MILLISECONDS)
      .withTimeout(10000, TimeUnit.MILLISECONDS);

    JavaScriptErrorListener listener = capturedErrorListener.getValue();
    HtmlPage page = createMock(HtmlPage.class);
    ScriptException ex;
    ex = new ScriptException(page, new IllegalStateException("Test"));
    listener.scriptException(page, ex);

    wait.start();
    assertThat(wait.getException(), is((Exception) ex));
    verify(client, options);
  }
}
