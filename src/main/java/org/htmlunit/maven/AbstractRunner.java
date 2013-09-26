package org.htmlunit.maven;

import java.beans.Statement;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.htmlunit.TypedPropertyEditor;
import org.htmlunit.javascript.EventHandler;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

/** Support for runners. It initializes configuration and provides utility
 * methods.
 *
 * <p>
 * When JavaScript is enabled, it supports three scripts loading phases:
 * bootstrap, sources and test files. For further information look at
 * {@link RunnerContext} documentation.
 * </p>
 *
 * <p>
 * If debug mode is enabled, it starts an HTTP server at
 * {@link RunnerContext#getDebugPort()} port instead of running each single
 * test.
 * </p>
 *
 * <p>
 * It generates a runner HTML file and writes it into the configured output
 * directory. Each test file will have a single runner file, and they will be
 * executed sequentially. Tests must close the window using the standard
 * <code>window.close()</code> method to indicate they finished. After finished
 * and before running the next test, this class invokes the
 * {@link #testFinished} method to let implementors perform validations and
 * clean actions. Implementations are responsible of loading each test into
 * the runner via {@link #loadTest}.
 * </p>
 */
public abstract class AbstractRunner implements WebDriverRunner {

  /** Class logger. */
  private static final Logger LOG = LoggerFactory
      .getLogger(AbstractRunner.class);

  /** Test runner file name. */
  private static final String TEST_RUNNER_SUFFIX = "Runner.html";

  /** Runner configuration; it's valid only after initialize(). */
  private RunnerContext context;

  /** Web driver to load pages; it's never null after initialize(). */
  private RunnerDriver driver;

  /** List of registered events. */
  private List<EventDefinition> eventDefinitions =
      new ArrayList<EventDefinition>();

  /** Object to wait for web driver processing; it's never null after
   * initialize(). */
  private WebClientWait wait;

  /** Loads a single test file into test runner template.
   *
   * @param runnerTemplate Current runner template. Cannot be null.
   * @param test Test to load, as a pattern. Cannot be null.
   */
  protected abstract void loadTest(final StringTemplate runnerTemplate,
      final URL test);

  /** {@inheritDoc}
   */
  @Override
  public void initialize(final RunnerContext theContext) {
    Validate.notNull(theContext, "The context cannot be null.");

    context = theContext;
    configureRunner(context);
    context.init();
    driver = new RunnerDriver(context.getBrowserVersion());
    int timeout = getContext().getTimeout();
    boolean throwException = driver.getWebClient().getOptions()
        .isThrowExceptionOnScriptError();
    wait = new WebClientWait(driver.getWebClient());
    wait.setThrowJavaScriptException(throwException)
      .pollingEvery(1000, TimeUnit.MILLISECONDS);
    if (timeout > -1) {
      // -1 means INFINITE, no timeout.
      wait.withTimeout(timeout, TimeUnit.SECONDS);
    }
  }

  /** {@inheritDoc}
   */
  @Override
  public void run() {
    if (getContext().isDebugMode()) {
      runServer();
    } else {
      runDriver();
    }
  }

  /** Adds an event listener to the current window, if any. The event will be
   * added to every new window.
   * <p>
   * Throws an exception if the runner isn't initialized.
   * </p>
   * @param eventType Event to listen (like "click").
   * @param handler Event listener. Cannot be null.
   * @param useCapture If true, uses event capture phase.
   */
  public void addEventListener(final String eventType,
      final EventHandler handler, final boolean useCapture) {
    Validate.notNull(driver, "The web client is not initialized.");

    if (driver.getWebClient().getCurrentWindow() != null
        && driver.getWebClient().getCurrentWindow().getScriptObject() != null) {
      Window window = (Window) driver.getWebClient().getCurrentWindow()
          .getScriptObject();
      window.addEventListener(eventType, handler, useCapture);
    }
    eventDefinitions.add(new EventDefinition(eventType, handler, useCapture));
  }

  /** Returns the runner configuration. Can be overriden to extend the default
   * context.
   *
   * @return Returns the current configuration, or null if this runner isn't
   *    yet initialized.
   */
  public RunnerContext getContext() {
    return context;
  }

  /** Returns the htmlunit web driver.
   * @return Returns the driver, or null if the runner isn't yet initialized.
   */
  public HtmlUnitDriver getDriver() {
    return driver;
  }

  /** {@inheritDoc}
   */
  @Override
  public String getName() {
    return getClass().getName();
  }

  /** Configures the runner. It's invoked during the runner initialization.
   * @param context Context to configure the runner. Cannot be null.
   */
  protected void configureRunner(final RunnerContext context) {
  }

  /** Allows to modify web client just after creation. By default it applies
   * all properties specified in the runner configuration. Some properties will
   * take no effect since they're used during construction.
   *
   * @param client Client to modify. Cannot be null.
   */
  protected void configureWebClient(final WebClient client) {
  }

  /** Invoked when a single test finished. Useful to validate results. It's not
   * supported when debug mode is enabled.
   *
   * @param test Test that finished. It's never null.
   * @param page DOM page which has the test results. It's never null.
   */
  protected void testFinished(final URL test, final HtmlPage page) {
  }

  /** Can be overridden in order to prepare the runner template before writing
   * to file and loading it into the web client.
   *
   * <p>
   * By default it performs replacement of {@link DefaultAttributes}.
   * </p>
   * @param testRunner Template already loaded. Cannot be null.
   */
  private void loadResources(final StringTemplate testRunner) {
    URL testRunnerScript = getContext().getTestRunnerScript();
    List<URL> bootstrapScripts = getContext().getBootstrapScripts();

    if (testRunnerScript != null) {
      testRunner.setAttribute("testRunnerScript",
          ResourceUtils.generateScriptTags(Arrays.asList(testRunnerScript)));
    }
    if (getContext().isDebugMode()) {
      bootstrapScripts.addAll(TestDebugServer
          .getDebugBootstrapScripts("localhost", getContext().getDebugPort()));
    }
    testRunner.setAttribute("bootstrapScripts",
        ResourceUtils.generateScriptTags(bootstrapScripts));
    testRunner.setAttribute("sourceScripts",
        ResourceUtils.generateScriptTags(getContext().getSourceScripts()));
  }

  /** Creates the test runner for the specified test and writes the processed
   * template to the runner. By default, the test runner will be named as the
   * test file plus a constant suffix.
   *
   * @param testFile Test script to create runner file for. Cannot be null.
   * @return The generated runner URL. Never returns null.
   */
  private URL createTestRunnerFile(final URL testFile) {
    String baseName = FilenameUtils.getBaseName(testFile.getFile());
    File runnerFile = new File(getContext().getOutputDirectory(),
        baseName + TEST_RUNNER_SUFFIX);

    // Generates a new template and prepares the environment.
    String htmlTemplate = ResourceUtils.readAsText(
        getContext().getTestRunnerTemplate());
    StringTemplate template = new StringTemplate(htmlTemplate,
        DefaultTemplateLexer.class);
    loadResources(template);

    // Loads test file into template.
    loadTest(template, testFile);

    try {
      FileUtils.writeStringToFile(runnerFile, template.toString());
      return runnerFile.toURI().toURL();
    } catch (IOException cause) {
      throw new RuntimeException("Cannot write runner file", cause);
    }
  }

  /** Runs tests using the web driver.
   *
   * @param runnerFile Runner file to load into the web driver. Cannot be null.
   * @param testFile Test file being executed. Cannot be null.
   */
  private void runDriver() {
    for (URL testFile : getContext().getTestFiles()) {
      URL runner = createTestRunnerFile(testFile);

      // Executes the test and waits for completion.
      getDriver().get(runner.toString());

      wait.start();

      // Notifies test result.
      testFinished(testFile, driver.getCurrentPage());

      // WebDriver doesn't switch automatically.
      String windowHandle = (String) CollectionUtils
          .get(getDriver().getWindowHandles(), 0);
      getDriver().switchTo().window(windowHandle);
    }
  }

  /** Runs tests using the a web server to allow debugging from browsers.
   *
   * @param runnerFile Runner file to load into the web driver. Cannot be null.
   * @param testFile Test file being executed. Cannot be null.
   */
  private void runServer() {
    try {
      int debugPort = getContext().getDebugPort();

      /** HTTP server for debug mode.
       */
      TestDebugServer debugServer = new TestDebugServer(debugPort) {
        /** {@inheritDoc}
         */
        @Override
        public URL getRunner(final URL testFile) {
          return createTestRunnerFile(testFile);
        }

        /** Adds the runner configuration to the window's global scope in order
         * to keep compatibility with non-debug tests.
         * <p>
         * {@inheritDoc}
         * </p>
         */
        @Override
        protected InputStream getDebugScript() {
          Set<Entry<Object, Object>> entries = getContext()
              .getRunnerConfiguration().entrySet();
          StringBuilder debugCode = new StringBuilder();

          for (Entry<Object, Object> entry : entries) {
            debugCode.append("window[\"" + entry.getKey() + "\"] = \""
                + entry.getValue() + "\";\n");
          }

          return new ByteArrayInputStream(debugCode.toString().getBytes());
        }
      };

      debugServer.setTestFiles(getContext().getTestFiles());
      debugServer.start();
    } catch (IOException cause) {
      throw new RuntimeException("Cannot start web server.", cause);
    }
  }

  /** Modified WebDriver with several workarounds and custom configuration.
   */
  class RunnerDriver extends HtmlUnitDriver {

    /** Creates a driver and sets the browser version.
     * @param version Driver's browser version. Cannot be null.
     */
    public RunnerDriver(final BrowserVersion version) {
      super(version);
    }

    /** Returns the {@link HtmlPage} for the current window.
     * @return When loaded, it returns a valid page.
     */
    public HtmlPage getCurrentPage() {
      return (HtmlPage) lastPage();
    }

    /** Returns the configured web client.
     * @return Returns a valid web client, never returns null.
     */
    @Override
    public WebClient getWebClient() {
      return super.getWebClient();
    }

    /** {@inheritDoc}
     */
    @Override
    protected WebClient modifyWebClient(final WebClient theClient) {
      theClient.setWebConnection(createConnectionWrapper(theClient));
      initializeWebClientConfiguration(theClient);
      configureWebClient(theClient);
      return theClient;
    };

    /** Since version 2.32.0, selenium doesn't allow to use DOM in closed
     * windows. We're currently assuming that closing window is the
     * signal of test completed, so it's necessary to check the DOM after
     * tests. We override the new selenium behaviour to make DOM work
     * with closed windows.
     */
    @Override
    protected Page lastPage() {
      return getCurrentWindow().getEnclosedPage();
    }

    /** Initializes default webclient configuration.
     *
     * @param theClient Client to initialize configuration. Cannot be null.
     */
    private void initializeWebClientConfiguration(final WebClient theClient) {
      // Loads default configuration from context.
      Properties configuration = getContext().getWebClientConfiguration();
      for (Object property : configuration.keySet()) {
        try {
          String methodName = "set" + StringUtils.capitalize((String) property);
          TypedPropertyEditor editor = new TypedPropertyEditor();
          editor.setValue(configuration.get(property));
          Statement stmt = new Statement(theClient.getOptions(), methodName,
              new Object[] { editor.getValue() });
          stmt.execute();
        } catch (Exception cause) {
          throw new IllegalArgumentException("Property " + property
              + " cannot be set in web client.", cause);
        }
      }

      theClient.setAjaxController(new NicelyResynchronizingAjaxController());
      theClient.setIncorrectnessListener(new IncorrectnessListener() {
        @Override
        public void notify(final String message, final Object origin) {
          LOG.trace(message, origin);
        }
      });
      theClient.addWebWindowListener(new WebWindowListener() {

        /** {@inheritDoc}
         */
        @Override
        public void webWindowOpened(final WebWindowEvent event) {
        }

        /** Adds registered event listeners to the window.
         * {@inheritDoc}
         */
        @Override
        public void webWindowContentChanged(final WebWindowEvent event) {
          com.gargoylesoftware.htmlunit.javascript.host.Window window;
          window = (com.gargoylesoftware.htmlunit.javascript.host.Window) event
              .getWebWindow().getScriptObject();
          registerEventListeners(window);
          publishConfiguration(window);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void webWindowClosed(final WebWindowEvent event) {
        }
      });
    }

    /** Creates a web connection that supports to load resources from the
     * classpath.
     *
     * @param client Client to wrap connection. Cannot be null.
     * @return A wrapped web connection, never returns null.
     */
    private WebConnection createConnectionWrapper(final WebClient client) {
      return new WebConnectionWrapper(client) {
        @Override
        public WebResponse getResponse(final WebRequest request)
            throws IOException {
          String protocol = request.getUrl().getProtocol();

          // Default web response is retrieved using commons HttpClient.
          // It assumes HttpClient created internally by HtmlUnit is using the
          // default registry. We check for supported schemes by the default
          // registry.
          boolean canHandle = SchemeRegistryFactory.createDefault()
              .get(protocol) != null;
          if (!canHandle) {
            // For unsupported schemes, it tries to read the response using
            // native URL connection.
            String data = ResourceUtils.readAsText(request.getUrl());
            return new StringWebResponse(data, request.getUrl());
          }
          return super.getResponse(request);
        }
      };
    }

    /** Adds all registered event listeners to the specified window.
     * @param window Window to add event listeners. Cannot be null.
     */
    private void registerEventListeners(
        final com.gargoylesoftware.htmlunit.javascript.host.Window window) {
      for (EventDefinition definition : eventDefinitions) {
        window.addEventListener(definition.getEventType(),
            definition.getHandler(), definition.isUseCapture());
      }
    }

    /** Publishes runner configuration properties to JavaScript's global scope.
     *
     * @param window Window to expose configuration. Cannot be null.
     */
    private void publishConfiguration(
        final com.gargoylesoftware.htmlunit.javascript.host.Window window) {
      Properties config = getContext().getRunnerConfiguration();

      for (Object key : config.keySet()) {
        window.defineProperty((String) key, config.get(key),
            ScriptableObject.READONLY);
      }
    }
  }

  /** Event definition to allow event enqueue.
   */
  private static class EventDefinition {
    /** Event to listen; it's never null. */
    private final String eventType;

    /** Event listener; it's never null. */
    private final EventHandler handler;

    /** True to use capture event phase. */
    private final boolean useCapture;

    /** Creates a new event definition.
     *
     * @param eventType Event to listen (like "click").
     * @param handler Event listener. Cannot be null.
     * @param useCapture If true, uses event capture phase.
     */
    public EventDefinition(final String theEventType,
        final EventHandler theHandler, final boolean isUseCapture) {
      eventType = theEventType;
      handler = theHandler;
      useCapture = isUseCapture;
    }

    /** Returns the event type.
     * @return A valid DOM event type. Never returns null.
     */
    public String getEventType() {
      return eventType;
    }

    /** Returns the event listener.
     * @return A valid JavaScript function. Never returns null.
     */
    public EventHandler getHandler() {
      return handler;
    }

    /** Determines whether to use the capture phase or not.
     * @return true to use the capture phase, false otherwise.
     */
    public boolean isUseCapture() {
      return useCapture;
    }
  }
}
