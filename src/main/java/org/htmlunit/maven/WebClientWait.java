package org.htmlunit.maven;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.google.common.base.Predicate;

/** Waits until all windows in a {@link WebClient} are closed or exception
 * is thrown in JavaScript.
 */
public class WebClientWait extends FluentWait<WebClient> {

  /** Web client to wait for; it's never null. */
  private final WebClient client;

  /** Since HtmlUnitWebDriver doesn't implement window handles, there's no
   * way to map the window being closed with a window in web driver, so
   * this map keep track of windows opened in the web driver. */
  private List<WebWindow> windows = new ArrayList<WebWindow>();

  /** Keeps the exception thrown in JavaScript. */
  private Exception exception;

  /** Indicates whether JavaScript exceptions will be thrown. If it's
   * false, exceptions will be saved but not thrown. */
  private boolean throwJavaScriptException;

  /** Creates a new wait object for the specified client.
   * @param theClient Client to wait for. Cannot be null.
   */
  public WebClientWait(final WebClient theClient) {
    super(theClient);

    Validate.notNull(theClient, "The web client cannot be null.");
    client = theClient;
    initialize(theClient);
  }

  /** Starts waiting until all windows in the web client are closed, or there
   * is an error in JavaScript. May throw an exception if
   * <code>throwJavaScriptException</code> is set to true.
   */
  public void start() {
    until(new Predicate<WebClient>() {
      public boolean apply(final WebClient input) {
        if (getException() != null && throwJavaScriptException) {
          throw new RuntimeException("JavaScript exception", getException());
        }
        return isDone();
      }
    });
  }

  /** Returns the web client to wait for.
   * @return A valid web client, never returns null.
   */
  public WebClient getWebClient() {
    return client;
  }

  /** Returns the exception thrown in JavaScript, if any.
   * @return The exception that caused this wait object to stop, or null
   *    if no one exist.
   */
  public Exception getException() {
    return exception;
  }

  /** Sets whether to throw JavaScript exceptions or not. If not, exception
   * will be available via {@link #getException()}.
   *
   * @param throwException true to throw exceptions, false otherwise.
   * @return Returns this object to continue settings options. Never returns
   *    null.
   */
  public WebClientWait setThrowJavaScriptException(
      final boolean throwException) {
    throwJavaScriptException = throwException;
    return this;
  }

  /** Indicates whether this wait finished or not.
   * @return True if finished, false otherwise.
   */
  boolean isDone() {
    return windows.size() == 0 || exception != null;
  }

  /** Initializes the web client to wait for, registering opened windows and
   * listening for new windows.
   * @param theClient Web client to wait. Cannot be null.
   */
  private void initialize(final WebClient theClient) {
    theClient.addWebWindowListener(webWindowListener);
    theClient.getOptions().setThrowExceptionOnScriptError(false);
    theClient.setJavaScriptErrorListener(javaScriptErrorListener);

    for (WebWindow webWindow : theClient.getWebWindows()) {
      if (!windows.contains(webWindow)
          && webWindow.getScriptObject() != null) {
        windows.add(webWindow);
      }
    }
  }

  /** Registers and unregisters windows. */
  private WebWindowListener webWindowListener = new WebWindowListener() {
    /** {@inheritDoc}
     */
    public void webWindowOpened(final WebWindowEvent event) {
    }

    /** {@inheritDoc}
     */
    public void webWindowContentChanged(final WebWindowEvent event) {
      WebWindow webWindow = event.getWebWindow();

      if (!windows.contains(webWindow)
          && webWindow.getScriptObject() != null) {
        windows.add(webWindow);
      }
    }

    public void webWindowClosed(final WebWindowEvent event) {
      windows.remove(event.getWebWindow());
    }
  };

  /** Intercepts all JavaScript exceptions. */
  private JavaScriptErrorListener javaScriptErrorListener =
      new JavaScriptErrorListener() {
    /** {@inheritDoc}
     */
    public void timeoutError(final HtmlPage htmlPage, final long allowedTime,
        final long executionTime) {
      exception = new TimeoutException("JavaScript timeout. Allowed time: "
          + allowedTime + ", Execution time: " + executionTime);
    }

    /** {@inheritDoc}
     */
    public void scriptException(final HtmlPage htmlPage,
        final ScriptException scriptException) {
      exception = scriptException;
    }

    /** {@inheritDoc}
     */
    public void malformedScriptURL(final HtmlPage htmlPage, final String url,
        final MalformedURLException malformedURLException) {
      exception = malformedURLException;
    }

    /** {@inheritDoc}
     */
    public void loadScriptError(final HtmlPage htmlPage, final URL scriptUrl,
        final Exception theException) {
      exception = theException;
    }
  };
}
