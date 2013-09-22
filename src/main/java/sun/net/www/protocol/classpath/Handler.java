package sun.net.www.protocol.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.lang.StringUtils;

/** Manages the classpath protocol.
 */
public class Handler extends URLStreamHandler {

  /** Returns a classpath resource url connection.
   *
   * {@inheritDoc}
   */
  @Override
  protected URLConnection openConnection(final URL url) throws IOException {
    String classPath = StringUtils.substringAfter(url.toString(), "classpath:");
    if (classPath.startsWith("/")) {
      classPath = classPath.substring(1);
    }
    URL resourceUrl = Thread.currentThread().getContextClassLoader()
        .getResource(classPath);

    if (resourceUrl == null) {
      throw new IOException("Classpath resource not found: " + url.toString());
    }

    return resourceUrl.openConnection();
  }
}
