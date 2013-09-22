package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.htmlunit.maven.AntExpression;
import org.htmlunit.maven.FileSystemScanner;
import org.htmlunit.maven.ResourceScanner;
import org.junit.Test;


/** Tests the {@link FileSystemScanner} class.
 */
public class FileSystemScannerTest {

  @Test
  public void list() throws Exception {
    File test = File.createTempFile("test", ".scanner");
    File tempDir = new File(System.getProperty("java.io.tmpdir"));

    AntExpression expression = new AntExpression("file:*.scanner");

    ResourceScanner scanner = new FileSystemScanner(tempDir, expression);
    List<URL> resources = scanner.list();
    test.delete();
    assertThat(resources.size() > 0, is(true));
    assertThat(resources.get(0).toString()
        .startsWith("file:" + tempDir.getCanonicalPath()), is(true));
  }

  @Test
  public void list_relativeNoRootDir() throws Exception {
    File tempDir = new File(".");
    File test = File.createTempFile("test", ".scanner", tempDir);

    AntExpression expression = new AntExpression("*.scanner");

    ResourceScanner scanner = new FileSystemScanner(expression);
    List<URL> resources = scanner.list();
    test.delete();
    assertThat(resources.size() > 0, is(true));
    assertThat(resources.get(0).toString()
        .startsWith("file:" + tempDir.getCanonicalPath()), is(true));
  }

  @Test
  public void list_relativeRootDir() throws Exception {
    File tempDir = new File(".", "foo");
    tempDir.mkdirs();
    File test = File.createTempFile("test", ".scanner", tempDir);

    AntExpression expression = new AntExpression("foo/*.scanner");

    ResourceScanner scanner = new FileSystemScanner(expression);
    List<URL> resources = scanner.list();
    test.delete();
    tempDir.delete();
    assertThat(resources.size() > 0, is(true));
    assertThat(resources.get(0).toString()
        .startsWith("file:" + tempDir.getCanonicalPath()), is(true));
  }
}
