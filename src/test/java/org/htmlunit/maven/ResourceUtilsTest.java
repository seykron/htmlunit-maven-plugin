package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/** Tests the {@link ResourceUtils} class.
 */
public class ResourceUtilsTest {

  @Test
  public void readAsText() throws IOException {
    File tempFile = File.createTempFile("foo", "bar");

    try {
      FileUtils.writeStringToFile(tempFile, "test");
      assertThat(ResourceUtils.readAsText(tempFile), is("test"));
      assertThat(ResourceUtils.readAsText(tempFile.toURI().toURL()),
          is("test"));
    } finally {
      tempFile.delete();
    }
  }

  @Test
  public void isJarResource() {
    String fileResource = "/org/htmlunit/maven/TestRunner.js";
    String jarResource = "/com/gargoylesoftware/htmlunit/WebWindow.class";

    assertThat(ResourceUtils.isJarResource(fileResource), is(false));
    assertThat(ResourceUtils.isJarResource(jarResource), is(true));
  }

  @Test
  public void expand() {
    String expr = "classpath:/org/htmlunit/maven/*.js";
    assertThat(ResourceUtils.expand(Arrays.asList(expr)).size(), is(7));
    assertThat(ResourceUtils.expand(expr).size(), is(7));
    assertThat(ResourceUtils.expand("").size(), is(0));
  }

  @Test
  public void generateScriptTags() throws Exception {
    List<URL> urls = ResourceUtils.expand("classpath:org/htmlunit/maven/*.js");
    String scriptTags = ResourceUtils.generateScriptTags(urls);
    StringBuilder expectedTags = new StringBuilder();

    for (URL source : urls) {
      expectedTags.append("<script type=\"text/javascript\" src=\"")
        .append(source).append("\"></script>");
    }

    assertThat(expectedTags.toString(), is(scriptTags));
  }
}
