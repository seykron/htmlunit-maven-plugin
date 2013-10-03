package org.htmlunit.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/** Matches a set of resources described by an {@link AntExpression}.
 */
public abstract class ResourceScanner {

  /** Convenience factory method to create a suitable resource scanner for the
   * specified expression. The resource must exist.
   *
   * @param expression Resource expression to scan. Cannot be null or
   *    empty.
   * @return Returns a valid scanner. Never returns null.
   */
  public static ResourceScanner create(final AntExpression expression) {

    if ("classpath".equals(expression.getProtocol())
        && ResourceUtils.isJarResource(expression.getRootDir())) {
      // Classpath resource located into a JAR file.
      return new ClassPathScanner(expression);
    } else {
      if ("classpath".equals(expression.getProtocol())) {
        // Classpath resource located in the file system.
        String rootDir = expression.getRootDir();
        if (rootDir.startsWith("/")) {
          rootDir = rootDir.substring(1);
        }

        URL url = Thread.currentThread().getContextClassLoader()
            .getResource(rootDir);

        Validate.notNull(url, "Resource not found.");

        return new FileSystemScanner(new File(url.getFile()), expression);
      } else if ("file".equals(expression.getProtocol())) {
        // Regular file system resource.
        return new FileSystemScanner(expression);
      } else {
        // Remote resource.
        return new RemoteResourceScanner(expression);
      }
    }
  }

  /** List resources matching the current expression.
   * @return Returns a list of valid resources. Never returns null.
   */
  public abstract List<URL> list();

  /** Expression to match; it's never null.  */
  private final AntExpression expression;

  /** Creates a new resource scanner to match the specified expression.
   *
   * @param theExpression Expression to match. Cannot be null.
   */
  public ResourceScanner(final AntExpression theExpression) {
    Validate.notNull(theExpression, "The expression cannot be null.");
    expression = theExpression;
  }

  /** Returns the expression to match.
   * @return Returns a valid expression. Never returns null.
   */
  public AntExpression getExpression() {
    return expression;
  }

  /** Scanner to support remote resource expressions.
   */
  private static class RemoteResourceScanner extends ResourceScanner {

    /** Creates the scanner and sets the related expression.
     * @param theExpression Remote url expression. Cannot be null.
     */
    public RemoteResourceScanner(final AntExpression theExpression) {
      super(theExpression);
    }

    /** {@inheritDoc}
     */
    @Override
    public List<URL> list() {
      String url = getExpression().getExpression();

      try {
        return Arrays.asList(new URL(url));
      } catch (MalformedURLException cause) {
        throw new RuntimeException("Cannot generate URL for remote resource: "
            + url, cause);
      }
    }
  }
}
