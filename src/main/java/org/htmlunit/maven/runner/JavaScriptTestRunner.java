package org.htmlunit.maven.runner;

import java.net.URL;
import java.util.Arrays;

import org.antlr.stringtemplate.StringTemplate;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.ResourceUtils;

/** Runner to execute tests in single JavaScript files.
 */
public class JavaScriptTestRunner extends AbstractRunner {

  /** Expands the pattern and loads resources as &lt;script&gt; tags into the
   * template.
   *
   * @param runnerTemplate Current runner template. Cannot be null.
   * @param test Test to load. Cannot be null.
   */
  @Override
  protected void loadTest(final StringTemplate runnerTemplate,
      final URL test) {
    runnerTemplate.setAttribute("testFiles",
        ResourceUtils.generateScriptTags(Arrays.asList(test)));
  }
}
