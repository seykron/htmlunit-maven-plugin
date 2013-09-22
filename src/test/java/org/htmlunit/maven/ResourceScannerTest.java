package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.htmlunit.maven.AntExpression;
import org.htmlunit.maven.ClassPathScanner;
import org.htmlunit.maven.FileSystemScanner;
import org.htmlunit.maven.ResourceScanner;
import org.junit.Test;


/** Tests the {@link ResourceScanner} class.
 */
public class ResourceScannerTest {

  @Test(expected = IllegalArgumentException.class)
  public void create_fail() {
    AntExpression expression;
    expression = new AntExpression("classpath:/i/dont/exist/**/*.js");

    ResourceScanner.create(expression);
  }

  @Test
  public void create_classpath() {
    AntExpression expression = new AntExpression(
        "classpath:/org/codehaus/plexus/util/**/*.class");

    ResourceScanner scanner = ResourceScanner.create(expression);
    assertThat(scanner, instanceOf(ClassPathScanner.class));
    assertThat(scanner.getExpression(), is(expression));
  }

  @Test
  public void create_classpathLocal() {
    AntExpression expression;
    expression = new AntExpression("classpath:/org/htmlunit/**/*.js");

    ResourceScanner scanner = ResourceScanner.create(expression);
    assertThat(scanner, instanceOf(FileSystemScanner.class));
    assertThat(scanner.getExpression(), is(expression));
  }

  @Test
  public void create_fileSystem() {
    AntExpression expression;
    expression = new AntExpression("file:org/htmlunit/**/*.js");

    ResourceScanner scanner = ResourceScanner.create(expression);
    assertThat(scanner, instanceOf(FileSystemScanner.class));
    assertThat(scanner.getExpression(), is(expression));
  }

  @Test
  public void create_remote() {
    AntExpression expression;
    expression = new AntExpression("http://foo.bar/test.js");

    ResourceScanner scanner = ResourceScanner.create(expression);
    assertThat(scanner.getExpression(), is(expression));
    assertThat(scanner.list().size(), is(1));
    assertThat(scanner.list().get(0).toString(), is("http://foo.bar/test.js"));
  }
}
