package org.htmlunit.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.logging.Log;
import org.htmlunit.TypedPropertyEditor;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/** Contains common {@link WebDriverRunner} configuration. By default
 * target browser version is {@link BrowserVersion#FIREFOX_17}.
 *
 * <p>
 * It also reads shared runner's configuration when {@link #init} is called.
 * </p>
 * <p>
 * {@link #getBootstrapScripts()}, {@link #getSourceScripts()},
 * {@link #getTestFiles()} and {@link #getTestRunnerScript()} are valid only
 * when JavaScript is enabled. Otherwise they're ignored.
 * </p>
 */
public class RunnerContext {

  /** Default test runner template. */
  public static final String DEFAULT_TEMPLATE =
      "classpath:/org/htmlunit/maven/DefaultTestRunner.html";

  /** Htmlunit browser version; it's never null. */
  private BrowserVersion browserVersion = BrowserVersion.FIREFOX_17;

  /** Properties to configure htmlunit web client; it's never null. */
  private Properties webClientConfiguration = new Properties();

  /** Properties to configure the runner; it's never null. */
  private Properties runnerConfiguration = new Properties();

  /** Timeout to wait for page loads. */
  private int timeout = -1;

  /** Maven logger. */
  private Log log;

  /** Indicates whether the runner must run in debug mode or not. */
  private boolean debugMode;

  /** Port to start debug server. Default is 8000. */
  private Integer debugPort = 8000;

  /** Path to the test runner template. */
  private URL testRunnerTemplate;

  /** Script included after page load in order to run the tests; can be
   * null. */
  private URL testRunnerScript;

  /** List of scripts loaded before test scripts; valid after initialize(). */
  private List<URL> bootstrapScripts = new ArrayList<URL>();

  /** List of scripts loaded before test scripts and after bootstrap
   * scripts; valid after initialize(). */
  private List<URL> sourceScripts = new ArrayList<URL>();

  /** Test files. Can be any resource identified as test by the current
   * runner. It's never null after initialize().
   */
  private List<URL> testFiles = new ArrayList<URL>();

  /** Test runner and test cases output directory; it's never null
   * after initialize();
   */
  private File outputDirectory;

  /** Default constructor, it initializes default values. */
  public RunnerContext() {
    try {
      testRunnerTemplate = new URL(DEFAULT_TEMPLATE);
    } catch (MalformedURLException cause) {
      throw new RuntimeException("Cannot initialize runner context.", cause);
    }
  }

  /** Initializes this context.
   */
  public void init() {
    readRunnerConfig(runnerConfiguration);
  }

  /** Returns the htmlunit browser version.
   * @return A valid browser version, never returns null.
   */
  public BrowserVersion getBrowserVersion() {
    return browserVersion;
  }

  /** Sets the htmlunit browser version.
   *
   * @param theBrowserVersion Browser version. Cannot be null.
   */
  public void setBrowserVersion(final BrowserVersion theBrowserVersion) {
    Validate.notNull(theBrowserVersion, "The browser version cannot be null.");
    browserVersion = theBrowserVersion;
  }

  /** Returns the HTMLUnit web client configuration.
   * @return A valid set of properties. Never returns null.
   */
  public Properties getWebClientConfiguration() {
    return webClientConfiguration;
  }

  /** Sets the HTMLUnit web client configuration.
   *
   * @param theWebClientConfiguration Web client configuration. Cannot be null.
   */
  public void setWebClientConfiguration(
      final Properties theWebClientConfiguration) {
    Validate.notNull(theWebClientConfiguration,
        "The web client configuration cannot be null.");
    webClientConfiguration = theWebClientConfiguration;
  }

  /** Returns the runner's specific configuration.
   * @return A valid set of properties. Never returns null.
   */
  public Properties getRunnerConfiguration() {
    return runnerConfiguration;
  }

  /** Sets the runner's specific configuration.
   *
   * @param theRunnerConfiguration Runner configuration. Cannot be null.
   */
  public void setRunnerConfiguration(
      final Properties theRunnerConfiguration) {
    Validate.notNull(theRunnerConfiguration,
        "The runner configuration cannot be null.");
    runnerConfiguration = theRunnerConfiguration;
  }

  /** Returns the timeout, in seconds, to wait for page load.
   * @return A number greater than 0, or -1 to wait infinite.
   */
  public int getTimeout() {
    return timeout;
  }

  /** Sets the timeout, in seconds, to wait for page load.
   *
   * @param theTimeout A number greater than 0, or -1 to wait infinite.
   */
  public void setTimeout(final int theTimeout) {
    timeout = theTimeout;
  }

  /** Returns the maven root logger.
   * @return A valid logger, or null if the logger isn't set.
   */
  public Log getLog() {
    return log;
  }

  /** Sets the maven root logger.
   *
   * @param theLog Maven's root logger. Cannot be null.
   */
  public void setLog(final Log theLog) {
    Validate.notNull(theLog, "The logger cannot be null.");
    log = theLog;
  }

  /** Sets whether the runner must run in debug mode or not.
   * @param isDebugMode True to run in debug mode, false otherwise.
   */
  public void setDebugMode(final boolean isDebugMode) {
    debugMode = isDebugMode;
  }

  /** Indicates whether the runner must run in debug mode or not.
   * @return Returns true to run in debug mode, false otherwise.
   */
  public boolean isDebugMode() {
    return debugMode;
  }

  /** Returns the debug port. Default is 8000.
   * @return A valid number.
   */
  public int getDebugPort() {
    return debugPort;
  }

  /** Determines whether JavaScript is enabled or not for this runner.
   * @return Returns <code>true</code> if JavaScript is enabled,
   *    <code>false</code> otherwise.
   */
  public boolean isJavaScriptEnabled() {
    return webClientConfiguration.containsKey("javaScriptEnabled") &&
        Boolean.valueOf(webClientConfiguration
            .getProperty("javaScriptEnabled"));
  }

  /** Returns the template used to build the test runner. Defaults to
   * {@link #DEFAULT_TEMPLATE}.
   *
   * @return Returns the current runner template, never returns null.
   */
  public URL getTestRunnerTemplate() {
    return testRunnerTemplate;
  }

  /** Returns the script used to initialize tests. It can be referenced in
   * the template via <code>$testRunnerScript$</code> placeholder.
   *
   * @return Returns the test runner script, or null if it isn't configured.
   */
  public URL getTestRunnerScript() {
    return testRunnerScript;
  }

  /** Returns the scripts usually used in the bootstrap phase. It can be
   * referenced in the template via <code>$bootstrapScripts$</code> placeholder.
   *
   * @return Returns the list of bootstrap scripts, or null if it isn't
   *    configured.
   */
  public List<URL> getBootstrapScripts() {
    return bootstrapScripts;
  }

  /** Returns the source scripts to test. It can be referenced in the template
   * via <code>$sourceScripts$</code> placeholder.
   *
   * @return Returns the list of JavaScript source scripts, or null if it isn't
   *    configured.
   */
  public List<URL> getSourceScripts() {
    return sourceScripts;
  }

  /** Returns the list of test files. It can be referenced in the template
   * via <code>$testFiles$</code> placeholder.
   *
   * @return Returns the list of test files, or null if it isn't configured.
   */
  public List<URL> getTestFiles() {
    return testFiles;
  }

  /** Returns the runners files and test results output directory.
   *
   * @return A valid directory. Never returns null after
   *  {@link #setRunnerConfiguration()}.
   */
  public File getOutputDirectory() {
    return outputDirectory;
  }

  /** Reads common runners' configuration from the current runner config.
   *
   * @param config Current runner's configuration. Cannot be null.
   */
  private void readRunnerConfig(final Properties config) {
    try {
      // Reads debug information.
      debugPort = readProperty(config, Integer.class, "debugPort", 8000);

      // Reads runner template.
      String template = readProperty(config, String.class, "testRunnerTemplate",
          null);
      if (template == null) {
        testRunnerTemplate = new URL(DEFAULT_TEMPLATE);
      } else {
        List<URL> urls = ResourceUtils.expand(template);

        if (urls.size() > 0) {
          testRunnerTemplate = urls.get(0);
        } else {
          testRunnerTemplate = new URL(template);
        }
      }

      // Reads javascript resources only if javascript is enabled.
      if (isJavaScriptEnabled()) {
        List<URL> runnerScriptFiles = expand(readProperty(config, String.class,
            "testRunnerScript", ""));
        if (runnerScriptFiles.size() > 0) {
          testRunnerScript = runnerScriptFiles.get(0);
        }
        bootstrapScripts = expand(readProperty(config, String.class,
            "bootstrapScripts", ""));
        sourceScripts = expand(readProperty(config, String.class,
            "sourceScripts", ""));
        testFiles = expand(readProperty(config, String.class, "testFiles", ""));
      }

      // Reads output directory.
      String output = readProperty(config, String.class, "outputDirectory", "");
      Validate.notEmpty(output,
          "The output directory cannot be null or empty.");
      outputDirectory = new File(output);
    } catch (Exception cause) {
      throw new RuntimeException("Error reading runner configuration.", cause);
    }
  }

  /** Reads a property and converts it to the native value.
   *
   * @param config Configuration to read. Cannot be null.
   * @param type Property type. Cannot be null.
   * @param key Property key. Cannot be null or empty.
   * @param defaultValue Default value if the property doesn't exist. Can be
   *    null.
   * @return Returns the required property, or the default value if it doesn't
   *    exist.
   */
  @SuppressWarnings("unchecked")
  private <T> T readProperty(final Properties config, final Class<T> type,
      final String key, final T defaultValue) {
    TypedPropertyEditor editor = new TypedPropertyEditor();

    if (config.containsKey(key)) {
      editor.setValue(config.getProperty(key));
      return (T) editor.getValue();
    }

    return defaultValue;
  }

  /** Expands the specified resource matching expression into real
   * resources, taking into account whether <code>debugMode</code> is active.
   * @param expression Expression to expand. Cannot be null.
   * @return A valid list of resources. Never returns null.
   */
  private List<URL> expand(final String expression) {
    List<URL> resources = ResourceUtils.expand(Arrays
        .asList(expression.split(";")));

    if (debugMode) {
      // In debug mode, all resources are served by the debug server.
      // It transforms resource urls into debug urls.
      List<URL> serverUrls = new ArrayList<URL>();

      for (URL resource : resources) {
        serverUrls.add(TestDebugServer.getStaticContentUrl("localhost",
            debugPort, resource));
      }
      return serverUrls;
    } else {
      return resources;
    }
  }
}
