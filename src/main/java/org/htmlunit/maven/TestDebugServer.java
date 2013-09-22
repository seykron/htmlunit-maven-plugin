package org.htmlunit.maven;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.htmlunit.NanoHTTPD;
import org.htmlunit.NanoHTTPD.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Debugging server that supports to serve static content. By default, when
 * no test is specified as parameter, it displays the list of available tests.
 *
 * <p>
 * Implementations must provide test runners when tests are required from
 * the client.
 * </p>
 * <p>
 * Runners must include debugging support scripts specified by
 * {@link #getDebugBootstrapScripts} as part of bootstrap phase.
 * </p>
 * <p>
 * Any external resource in the runner must be handled by the debug server, so
 * they should be transformed via {@link #getStaticContentUrl}.
 * </p>
 * <p>
 * Implementations may provide additional debugging code generated in runtime
 * via {@link #getDebugScript()}.
 * </p>
 * <p>
 * This server supports additional request handlers implementing the following
 * rules:
 *   <ul>
 *     <li>Request uri <code>/action/</code> will be handled by a public
 *     instance method named
 *     <code>handleAction(String uri, Map<String, String> params)</code>.</li>
 *     <li>Request handler method must return a valid {@link Response}.</li>
 *   </ul>
 * </p>
 * <p>
 * The default request handler (endpoint <code>/</code>) can be changed
 * overriding {@link #handleDefault}.
 * </p>
 * <p>
 * It notifies when browser window is closed via {@link #handleDisconnect} (or
 * by issuing a HTTP GET request to <code>/disconnect/</code>.
 * </p>
 */
public abstract class TestDebugServer extends NanoHTTPD {

  /** Test handler endpoint. */
  public static final String TEST_ENDPOINT = "/test/";

  /** Class logger. */
  private static final Logger LOG = LoggerFactory
      .getLogger(TestDebugServer.class);

  /** Static content request parameter. */
  private static final String STATIC_CONTENT_PARAM = "url";

  /** Static content endpoint. */
  private static final String STATIC_CONTENT_ENDPOINT = "/static/";

  /** Debug support file, should be added to runners in order to
   * support debugging. */
  private static final String DEBUG_SUPPORT =
      "classpath:/org/htmlunit/maven/TestDebug.js";

  /** Takes an url and returns the debug url that will be handled by a
   * debug server. Runners must contains transformed urls in order to let
   * debug server to handle all resources.
   *
   * @param debugHost Host name where debug server is listening. Cannot be null
   *    or empty.
   * @param debugPort Port where debug server is listening. Must be greater
   *    than 0.
   * @return Returns a list of script urls. Never returns null.
   */
  public static final URL getStaticContentUrl(final String debugHost,
      final int debugPort, final URL original) {
    Validate.notEmpty(debugHost, "The debug host cannot be null or empty.");
    Validate.isTrue(debugPort > 0, "The debug port must be greater than 0.");
    Validate.notNull(original, "The source url cannot be null.");

    try {
      return new URL("http://" + debugHost + ":" + debugPort
          + STATIC_CONTENT_ENDPOINT + "?" + STATIC_CONTENT_PARAM + "=" +
          original.toString());
    } catch (MalformedURLException cause) {
      throw new RuntimeException("Cannot generate debug url", cause);
    }
  }

  /** Returns the list of bootstrap sources that must be included by runners
   * in order to make debug server work.
   *
   * @param debugHost Host name where debug server is listening. Cannot be null
   *    or empty.
   * @param debugPort Port where debug server is listening. Must be greater
   *    than 0.
   * @return Returns a list of script urls. Never returns null.
   */
  public static final List<URL> getDebugBootstrapScripts(
      final String debugHost, final int debugPort) {
    try {
      return Arrays.asList(new URL("http://" + debugHost + ":" + debugPort
          + STATIC_CONTENT_ENDPOINT + "?" + STATIC_CONTENT_PARAM +
          "=" + DEBUG_SUPPORT));
    } catch (MalformedURLException cause) {
      throw new RuntimeException("Cannot generate debug file url.", cause);
    }
  }

  /** Returns the runner for the specified test file. It's invoked when a
   * test is required from the browser.
   *
   * @param testFile Test file related to required runner. Cannot be null.
   * @return Returns the test runner url, ready to be executed. Never returns
   *    null.
   */
  protected abstract URL getRunner(final URL testFile);

  /** List of available test files; it's never null.
   */
  private List<URL> testFiles;

  /** Creates the debug server to listen in the specified port of localhost.
   *
   * @param port Port to listen. Must be greater than 0.
   */
  public TestDebugServer(final int port) {
    super(port);
  }

  /** Creates the debug server to listen in the specified port of localhost.
   *
   * @param port Port to listen. Must be greater than 0.
   * @param theTestFiles List of available tests. Cannot be null.
   */
  public TestDebugServer(final int port, final List<URL> theTestFiles) {
    super(port);
    Validate.notNull(theTestFiles, "The test files cannot be null.");

    testFiles = theTestFiles;
  }

  /** Delegates to methods by the following naming convention:
   *
   * <p>
   *   <code>/action/some/other/path?with=params</code>
   *   will result in invokation of:
   *   <code>handleAction(uri, params);</code>
   * </p>
   *
   * The first path element upper-cased preceded by <code>handle</code>.
   *
   * {@inheritDoc}
   */
  @Override
  public Response serve(final String uri, final Method method,
      final Map<String, String> header, final Map<String, String> params,
      final Map<String, String> files) {

    if (uri.equals("/")) {
      // Default action.
      return handleDefault(params);
    }

    if (uri.lastIndexOf("/") == 0) {
      return new Response(Status.NOT_FOUND, "text/plain", "Not found");
    }

    String action = StringUtils.capitalize(StringUtils
        .substringBefore(uri.substring(1), "/"));
    try {
      java.lang.reflect.Method handler = getClass().getMethod("handle" + action,
          new Class[] { String.class, Map.class });
      handler.setAccessible(true);
      return (Response) handler.invoke(this, uri, params);
    } catch (Exception cause) {
      LOG.debug("Action not found", cause);
      return new Response(Status.NOT_FOUND, "text/plain", "Not found");
    }
  }

  /** Sets the test files managed by this server.
   * @param theTestFiles List of test files. Cannot be null.
   */
  public void setTestFiles(final List<URL> theTestFiles) {
    Validate.notNull(theTestFiles, "Test files cannot be null.");
    testFiles = theTestFiles;
  }

  /** Handles default request (<code>/</code>).
   * @param params Parameters provided to the default request. Cannot be null.
   * @return Returns the server response. Never returns null.
   */
  public Response handleDefault(final Map<String, String> params) {
    Validate.notNull(params, "The request parameters cannot be null.");

    StringBuilder builder = new StringBuilder();

    for (URL test : testFiles) {
      builder.append(TEST_ENDPOINT + test.toString());
      builder.append("\n");
    }

    return new Response(Status.OK, "text/plain", builder.toString());
  }

  /** Handles static content request (<code>/static/</code>).
   * @param uri Request uri. Cannot be null or empty.
   * @param params Parameters provided to the request. Cannot be null.
   * @return Returns the server response. Never returns null.
   */
  public Response handleStatic(final String uri,
      final Map<String, String> params) {
    Validate.notEmpty(uri, "The request uri cannot be null or empty.");
    Validate.notNull(params, "The request parameters cannot be null.");

    try {
      String sourceCode = "";

      if (params.get(STATIC_CONTENT_PARAM).endsWith(DEBUG_SUPPORT)
          && getDebugScript() != null) {
        sourceCode += ResourceUtils.readAsText(getDebugScript());
      }

      URL url = new URL(params.get(STATIC_CONTENT_PARAM));
      sourceCode += ResourceUtils.readAsText(url);

      return new Response(Status.OK, MimetypesFileTypeMap
          .getDefaultFileTypeMap().getContentType(url.toString()), sourceCode);
    } catch (Exception cause) {
      LOG.debug("Cannot read resource data.", cause);
      return new Response(Status.NOT_FOUND, "text/plain", "Not found");
    }
  }

  /** Handles requests to load test runners (<code>/test/</code>).
   *
   * @param uri Request uri. Cannot be null or empty.
   * @param params Parameters provided to the request. Cannot be null.
   * @return Returns the server response. Never returns null.
   */
  public Response handleTest(final String uri,
      final Map<String, String> params) {
    Validate.notEmpty(uri, "The request uri cannot be null or empty.");
    Validate.notNull(params, "The request parameters cannot be null.");

    URL test = lookupTest(StringUtils.substringAfter(uri, TEST_ENDPOINT));
    if (test == null) {
      return new Response(Status.NOT_FOUND, "text/plain", "TEST NOT FOUND.");
    }
    URL runner = getRunner(test);
    return new Response(Status.OK, "text/html",
        ResourceUtils.readAsText(runner));
  }

  /** Shutdowns the server (<code>/disconnect/</code>).
   *
   * @param uri Request uri. Cannot be null or empty.
   * @param params Parameters provided to the request. Cannot be null.
   * @return Returns the server response. Never returns null.
   */
  public Response handleDisconnect(final String uri,
      final Map<String, String> params) {
    Validate.notEmpty(uri, "The request uri cannot be null or empty.");
    Validate.notNull(params, "The request parameters cannot be null.");

    return new Response("Bye");
  }

  /** Allows to add debugging code to tests. Debugging code is included
   * just before debug support scripts specified by
   * {@link #getDebugBootstrapScripts}.
   *
   * @return Returns a valid input stream to read debugging code, or null if
   *    not additional debug code is needed. If provided, input stream is
   *    closed after read.
   */
  protected InputStream getDebugScript() {
    return null;
  }

  /** Searches for a test that matches the specified url.
   *
   * @param uri Url to match tests. Cannot be null or empty.
   * @return Returns the matching test, or <code>null</code> if no one
   *    matches.
   */
  private URL lookupTest(final String uri) {
    for (URL test : testFiles) {
      if (test.toString().endsWith(uri)) {
        return test;
      }
    }

    return null;
  }
}
