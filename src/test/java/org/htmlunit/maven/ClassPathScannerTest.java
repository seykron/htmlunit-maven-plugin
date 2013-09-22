package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.List;

import org.htmlunit.maven.AntExpression;
import org.htmlunit.maven.ClassPathScanner;
import org.htmlunit.maven.ResourceScanner;
import org.junit.Before;
import org.junit.Test;


/** Tests the {@link ClassPathScanner} class.
 */
public class ClassPathScannerTest {

  @Before
  public void setUp() {
    System.setProperty("java.protocol.handler.pkgs",
        "com.ffsocial.htmlunit.protocol");
  }

  @Test
  public void list() {
    AntExpression expression = new AntExpression(
        "classpath:/org/codehaus/plexus/util/**/*.class");

    ResourceScanner scanner = new ClassPathScanner(expression);
    List<URL> resources = scanner.list();
    assertThat(resources.size() > 0, is(true));
    assertThat(resources.get(0).toString()
        .startsWith("classpath:org/codehaus/plexus/util"), is(true));
  }
}
