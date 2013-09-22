package org.htmlunit.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

/** Utilities to manage resources.
 */
public final class ResourceUtils {

  /** Cannot be created. */
  private ResourceUtils() {
  }

  /** Reads the specified file as text. It uses the default charset.
   * @param file File to read. Cannot be null.
   * @return The file content as text.
   */
  public static String readAsText(final File file) {
    Validate.notNull(file, "The file cannot be null.");
    try {
      return FileUtils.readFileToString(file);
    } catch (IOException cause) {
      throw new RuntimeException("Cannot read file.", cause);
    }
  }

  /** Reads the specified url into text.
   *
   * It uses the system proxy if needed.
   *
   * @param url Url to read. Cannot be null.
   * @return The URL content as String. Never returns null.
   */
  public static String readAsText(final URL url) {
    Validate.notNull(url, "The url cannot be null.");
    try {
      return readAsText(url.openStream());
    } catch (IOException cause) {
      throw new RuntimeException("Cannot read URL: " + url.toString(), cause);
    }
  }

  /** Reads the specified input stream as text.
   *
   * @param input Input stream to read. Cannot be null.
   * @return The input stream content as String. Never returns null.
   */
  public static String readAsText(final InputStream input) {
    Validate.notNull(input, "The input stream cannot be null.");
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      IOUtils.copy(input, output);
      return output.toString();
    } catch (IOException cause) {
      throw new RuntimeException("Cannot read input stream.", cause);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }

  /** Determines whether the specified classpath resource is located inside a
   * a JAR file. It uses thread context class loader to locate resource.
   *
   * @param resource Fully-qualified classpath resource. Cannot be null or
   *    empty, and it must exist.
   * @return Returns true if the url is a JAR resource, false otherwise.
   */
  public static boolean isJarResource(final String resource) {
    Validate.notEmpty(resource, "The url cannot be null.");
    String classPath = resource;

    if (classPath.startsWith("/")) {
      classPath = classPath.substring(1);
    }
    URL url = Thread.currentThread().getContextClassLoader()
        .getResource(classPath);
    Validate.notNull(url, "Resource " + resource + " not found.");

    try {
      return JarURLConnection.class.isInstance(url.openConnection());
    } catch (IOException cause) {
      throw new RuntimeException(
          "Cannot open connection to check JAR resource.", cause);
    }
  }

  /** Expands a single resource expression into physical resources.
   * It performs the logical disjunction of exclusion patterns and returns
   * only included resources.
   *
   * @param expression Resource expression to expand. Cannot be null.
   * @return A valid list of resources. Never returns null.
   */
  public static List<URL> expand(final String expression) {
    return expand(Arrays.asList(expression));
  }

  /** Expands a set of resource expressions into physical resources.
   * It performs the logical disjunction of exclusion patterns and returns
   * only included resources.
   *
   * @param expressions List of resource expressions to expand. Cannot be null.
   * @return A valid list of resources. Never returns null.
   */
  @SuppressWarnings("unchecked")
  public static List<URL> expand(final List<String> expressions) {
    List<URL> includes = new ArrayList<URL>();
    List<URL> excludes = new ArrayList<URL>();

    for (String resourceExpression : expressions) {
      if (!resourceExpression.isEmpty()) {
        AntExpression expression = new AntExpression(resourceExpression);
        ResourceScanner scanner = ResourceScanner.create(expression);

        if (expression.isExclusion()) {
          excludes.addAll(scanner.list());
        } else {
          includes.addAll(scanner.list());
        }
      }
    }

    return (List<URL>) CollectionUtils.subtract(includes, excludes);
  }

  /** Generates a list of HTML script tags for the specified list of sources.
   * @param sources JavaScript source files. Cannot be null.
   * @return A valid HTML, never returns null.
   */
  public static String generateScriptTags(final List<URL> sources) {
    StringBuilder scripts = new StringBuilder();
    for (URL source : sources) {
      scripts.append("<script type=\"text/javascript\" src=\"")
        .append(source).append("\"></script>");
    }
    return scripts.toString();
  }
}
