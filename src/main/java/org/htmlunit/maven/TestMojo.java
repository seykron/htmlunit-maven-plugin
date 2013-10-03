package org.htmlunit.maven;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.htmlunit.maven.runner.JavaScriptTestRunner;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/** Base class to execute test runner mojos. By default it configures and
 * executes the runner.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST,
  requiresDependencyResolution = ResolutionScope.TEST)
public class TestMojo extends AbstractMojo {

  /** Maven's artifact resolver. */
  @Component
  private ArtifactResolver artifactResolver;

  /** Provides some metadata operations, like querying the remote repository for
   * a list of versions available for an artifact.
   */
  @Component
  private ArtifactMetadataSource metadataSource;

  /** Specifies the repository used for artifact handling.
   */
  @Parameter(defaultValue = "${localRepository}")
  private ArtifactRepository localRepository;

  /** The Maven project object, used to generate a classloader to access the
   * classpath resources from the project.
   *
   * Injected by maven. This is never null.
   */
  @Component
  private MavenProject project;

  /** Determines the web driver runner class. By default {@link HtmlUnitDriver}
   * is used.
   */
  @Parameter
  private String runnerClassName;

  /** Properties to configure the runner.
   */
  @Parameter(required = true)
  private Map<String, String> runnerConfiguration;


  /** Properties to configure the {@link WebClient}.
   */
  @Parameter
  private Map<String, String> webClientConfiguration;

  /** Indicates if dependencies will be added to the current thread class
   * loader.
   */
  @Parameter
  private boolean dependenciesClassLoader;

  /** Indicates if test dependencies will be added to the current thread class
   * loader.
   */
  @Parameter
  private boolean testDependenciesClassLoader;

  /**
   * Determines the browser and version profile that HtmlUnit will simulate.
   * This setting does nothing if the plugin is configured not to use HtmlUnit.
   * This maps 1-to-1 with the public static instances found in
   * {@link com.gargoylesoftware.htmlunit.BrowserVersion}.
   *
   * Some valid examples: FIREFOX_3_6, INTERNET_EXPLORER_6, INTERNET_EXPLORER_7,
   * INTERNET_EXPLORER_8
   */
  @Parameter(defaultValue = "FIREFOX_17")
  private String browserVersion;

  /** Web client page load timeout, in seconds.
   */
  @Parameter(defaultValue = "30")
  private int timeout;

  /** Indicates whether to skip tests or not. Runners won't be processed
   * if tests are skipped.
   *
   * @parameter expression="${skipTests}"
   */
  @Parameter(property = "skipTests")
  private boolean skipTests;

  /** Enables test debugging. It starts a server and allows to connect from
   * a browser.
   */
  @Parameter(property = "maven.surefire.debug")
  private boolean debugMode;

  /** List of properties to register in {@link System#getProperties()}.
   */
  @SuppressWarnings("rawtypes")
  @Parameter
  private Map systemProperties = new HashMap();

  /** Executes jasmine tests.
   *
   * @throws MojoExecutionException On plugin internal error.
   * @throws MojoFailureException On plugin failure.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipTests) {
      return;
    }

    registerSystemProperties();

    WebDriverRunner runner = createRunner();
    RunnerContext context = new RunnerContext();
    context.setBrowserVersion(getBrowserVersion());

    if (runnerConfiguration != null) {
      Properties runnerProperties = new Properties();
      runnerProperties.putAll(runnerConfiguration);
      context.setRunnerConfiguration(runnerProperties);
    }

    if (webClientConfiguration != null) {
      Properties webClientProperties = new Properties();
      webClientProperties.putAll(webClientConfiguration);
      context.setWebClientConfiguration(webClientProperties);
    }

    context.setTimeout(timeout);
    context.setLog(getLog());
    context.setDebugMode(debugMode);

    try {
      getLog().info("Initializing " + runner.getName());
      runner.initialize(context);
      doExecute(runner);
    } catch (RuntimeException cause) {
      throw new MojoExecutionException("Error executing htmlunit.", cause);
    }
  }

  /** Executes the specified runner. By default it just executes the
   * runner.
   *
   * @param runner Runner to execute. It's never null.
   * @throws MojoExecutionException On plugin internal error.
   * @throws MojoFailureException On plugin failure.
   */
  protected void doExecute(final WebDriverRunner runner)
      throws MojoExecutionException, MojoFailureException {
    runner.run();
  }


  /** Creates the web driver to load pages. It uses the factory class if
   * it was specified, or creates the default web driver otherwise.
   *
   * @return a valid web driver, never returns null.
   */
  @SuppressWarnings("unchecked")
  private WebDriverRunner createRunner() {
    ClassLoader classLoader = createDependenciesClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);

    if (runnerClassName != null) {
      try {
        Class<? extends WebDriverRunner> klass;
        klass = (Class<? extends WebDriverRunner>) classLoader
            .loadClass(runnerClassName);
        Constructor<? extends WebDriverRunner> ctor = klass.getConstructor();
        return ctor.newInstance();
      } catch (Exception cause) {
        throw new RuntimeException("Couldn't instantiate runnerClassName",
            cause);
      }
    }

    return new JavaScriptTestRunner();
  }

  /** Creates a {@link ClassLoader} which contains all the project's
   * dependencies.
   *
   * @return Returns the created {@link ClassLoader} containing all the
   *    project's dependencies.
   */
  private ClassLoader createDependenciesClassLoader() {
    ClassLoaderBuilder builder = new ClassLoaderBuilder(artifactResolver,
        metadataSource, localRepository, project);
    ClassLoader classLoader = builder
        .includeDependencies(dependenciesClassLoader)
        .includeTestDependencies(testDependenciesClassLoader)
        .setParent(Thread.currentThread().getContextClassLoader())
        .create();
    registerContextUrlStreamHandlerFactory(classLoader);

    return classLoader;
  }

  /** Registers a url handler factory to resolve stream handlers loaded
   * by the specified class loader.
   *
   * @param classLoader Class loader that loaded stream handlers. Cannot be
   *    null.
   */
  private void registerContextUrlStreamHandlerFactory(
      final ClassLoader classLoader) {
    URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
      @Override
      @SuppressWarnings("unchecked")
      public URLStreamHandler createURLStreamHandler(final String protocol) {
        try {
          Class<? extends URLStreamHandler> handlerClass;
          handlerClass = (Class<? extends URLStreamHandler>) classLoader
              .loadClass("sun.net.www.protocol." + protocol + ".Handler");
          Constructor<? extends URLStreamHandler> ctor;
          ctor = handlerClass.getConstructor();
          return ctor.newInstance();
        } catch (Exception cause) {
          throw new RuntimeException("Cannot resolve stream handler.", cause);
        }
      }
    });
  }

  /** Determines the htmlunit browser version to use.
   * @return A valid browser version, never returns null.
   */
  private BrowserVersion getBrowserVersion() {
    BrowserVersion driverBrowserVersion;
    try {
      driverBrowserVersion = (BrowserVersion) BrowserVersion.class
          .getField(browserVersion.toUpperCase()).get(BrowserVersion.class);
    } catch (Exception cause) {
      throw new RuntimeException(cause);
    }
    return driverBrowserVersion;
  }


  /** Registers system properties in {@link System#getProperties()} so custom
   * {@link WebDriver}s are able to get context from the current execution.
   */
  private void registerSystemProperties() {
    for (Object key : systemProperties.keySet()) {
      System.setProperty(String.valueOf(key),
          String.valueOf(systemProperties.get(key)));
    }
  }
}
