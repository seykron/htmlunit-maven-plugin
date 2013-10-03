package org.htmlunit.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.codehaus.plexus.util.DirectoryScanner;

/** Scanner that matches files in the file system.
 */
public class FileSystemScanner extends ResourceScanner {

  /** Directory to start scanning; it's never null. */
  private final File baseDir;

  /** Creates a new file system scanner and sets the base directory.
   *
   * @param theBaseDir Directory to scan. Cannot be null.
   * @param expression Expression to match. Cannot be null.
   */
  public FileSystemScanner(final File theBaseDir,
      final AntExpression expression) {
    super(expression);
    Validate.notNull(theBaseDir, "The base directory cannot be null.");
    baseDir = theBaseDir;
  }

  /** Creates a new file system scanner and uses the expression root directory.
   *
   * @param expression Expression to match. Cannot be null.
   */
  public FileSystemScanner(final AntExpression expression) {
    super(expression);
    String rootDir = expression.getRootDir();
    if (rootDir.isEmpty()) {
      // Assumes current directory.
      rootDir = ".";
    }
    baseDir = new File(rootDir);
  }

  /** Scans the base directory to search for files matching the current
   * expression.
   *
   * <p>{@inheritDoc}</p>
   */
  @Override
  public List<URL> list() {
    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(baseDir);
    scanner.setIncludes(new String[] {getExpression().getPattern()});
    scanner.scan();

    try {
      List<URL> resources = new LinkedList<URL>();
      for (String file : scanner.getIncludedFiles()) {
        resources.add(new File(baseDir, file).toURI().toURL());
      }
      return resources;
    } catch (MalformedURLException cause) {
      throw new RuntimeException("Cannot map file to url.", cause);
    }
  }
}
