package org.htmlunit.maven.runner;

import java.net.URL;

import org.antlr.stringtemplate.StringTemplate;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.ResourceUtils;

/** Runner to load tests from plain HTML files.
 */
public class HtmlTestRunner extends AbstractRunner {

  /** Considers the test file as HTML resource and loads the mark-up into the
   * template.
   * <p>{@inheritDoc}</p>
   */
  @Override
  protected void loadTest(final StringTemplate runnerTemplate,
      final URL test) {
    String htmlTest = ResourceUtils.readAsText(test);
    runnerTemplate.setAttribute("testFiles",
        htmlTest);
  }
}
