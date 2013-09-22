package org.htmlunit.maven;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.collections.EnumerationUtils;
import org.codehaus.plexus.util.SelectorUtils;

/** Scans the classpath to match resources.
 */
public class ClassPathScanner extends ResourceScanner {

  public ClassPathScanner(final AntExpression theExpression) {
    super(theExpression);
  }

  /** Find all resources in jar files that match the given location pattern
   * via the Ant-style matcher.
   */
  @Override
  public List<URL> list() {
    String rootDir = getExpression().getRootDir();

    if (rootDir.startsWith("/")) {
      rootDir = rootDir.substring(1);
    }

    Enumeration<URL> urls;
    try {
      urls = Thread.currentThread().getContextClassLoader()
          .getResources(rootDir);
    } catch (IOException cause) {
      throw new RuntimeException("Cannot list resources.", cause);
    }
    Set<URL> resources = new HashSet<URL>();

    for (Object url : EnumerationUtils.toList(urls)) {
      resources.addAll(findMatches((URL) url));
    }
    return new LinkedList<URL>(resources);
  }

  /** Searches for expression matches in the specified JAR resource.
   * @param url URL to the JAR resource. Cannot be null.
   * @return Returns a list of matching resources. Never returns null.
   */
  private List<URL> findMatches(final URL url) {
    JarFile jarFile;
    String rootEntryPath;
    try {
      URLConnection con = url.openConnection();

      if (!JarURLConnection.class.isInstance(con)) {
        // It's not a JAR resource. Skipping.
        return new LinkedList<URL>();
      }

      // Should usually be the case for traditional JAR files.
      JarURLConnection jarCon = (JarURLConnection) con;
      jarCon.setUseCaches(false);
      jarFile = jarCon.getJarFile();
      JarEntry jarEntry = jarCon.getJarEntry();
      rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");

      if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
        // Root entry path must end with slash to allow for proper matching.
        // The Sun JRE does not return a slash here, but BEA JRockit does.
        rootEntryPath = rootEntryPath + "/";
      }

      List<URL> result = new LinkedList<URL>();
      Enumeration<JarEntry> entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String entryPath = entry.getName();
        if (entryPath.startsWith(rootEntryPath)) {
          String relativePath = entryPath.substring(rootEntryPath.length());
          if (SelectorUtils.matchPath(getExpression().getPattern(),
              relativePath)) {
            result.add(new URL("classpath:" + rootEntryPath + relativePath));
          }
        }
      }

      return result;
    } catch (IOException cause) {
      throw new RuntimeException(cause);
    }
  }
}
