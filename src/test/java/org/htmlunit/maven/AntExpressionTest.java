package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.htmlunit.maven.AntExpression;
import org.junit.Test;


/** Tests the {@link AntExpression} class.
 */
public class AntExpressionTest {

  @Test
  public void protocol() {
    AntExpression expression1 = new AntExpression("  classpath:/foo/**/ba?  ");
    AntExpression expression2 = new AntExpression("  ~classpath:/foo/**/ba?  ");
    AntExpression expression3 = new AntExpression("  /foo/**/ba?  ");
    AntExpression expression4 = new AntExpression("  ~/foo/**/ba?  ");
    AntExpression expression5 = new AntExpression("  **/ba?  ");
    AntExpression expression6 = new AntExpression("  ~**/ba?  ");

    assertThat(expression1.getProtocol(), is("classpath"));
    assertThat(expression2.getProtocol(), is("classpath"));
    assertThat(expression3.getProtocol(), is(nullValue()));
    assertThat(expression4.getProtocol(), is(nullValue()));
    assertThat(expression5.getProtocol(), is(nullValue()));
    assertThat(expression6.getProtocol(), is(nullValue()));
  }

  @Test
  public void rootDir() {
    AntExpression expression1 = new AntExpression("  classpath:/foo/**/ba?  ");
    AntExpression expression2 = new AntExpression("  ~classpath:foo/**/ba?  ");
    AntExpression expression3 = new AntExpression("  /foo/**/ba?  ");
    AntExpression expression4 = new AntExpression("  ~/foo/**/ba?  ");
    AntExpression expression5 = new AntExpression("  **/ba?  ");
    AntExpression expression6 = new AntExpression("  ~**/ba?  ");
    AntExpression expression7 = new AntExpression("  foo/bar/test.ext  ");

    assertThat(expression1.getRootDir(), is("/foo"));
    assertThat(expression2.getRootDir(), is("foo"));
    assertThat(expression3.getRootDir(), is("/foo"));
    assertThat(expression4.getRootDir(), is("/foo"));
    assertThat(expression5.getRootDir(), is(""));
    assertThat(expression6.getRootDir(), is(""));
    assertThat(expression7.getRootDir(), is("foo/bar"));
  }

  @Test
  public void pattern() {
    AntExpression expression1 = new AntExpression("  classpath:/foo/**/ba?  ");
    AntExpression expression2 = new AntExpression("  ~classpath:/foo/**/ba?  ");
    AntExpression expression3 = new AntExpression("  /foo/**/ba?  ");
    AntExpression expression4 = new AntExpression("  ~/foo/**/ba?  ");
    AntExpression expression5 = new AntExpression("  **/ba?  ");
    AntExpression expression6 = new AntExpression("  ~**/ba?  ");
    AntExpression expression7 = new AntExpression("  foo/bar/test.ext  ");

    assertThat(expression1.getPattern(), is("**/ba?"));
    assertThat(expression2.getPattern(), is("**/ba?"));
    assertThat(expression3.getPattern(), is("**/ba?"));
    assertThat(expression4.getPattern(), is("**/ba?"));
    assertThat(expression5.getPattern(), is("**/ba?"));
    assertThat(expression6.getPattern(), is("**/ba?"));
    assertThat(expression7.getPattern(), is("test.ext"));
  }

  @Test
  public void exclusion() {
    AntExpression expression1 = new AntExpression("  classpath:/foo/**/ba?  ");
    AntExpression expression2 = new AntExpression("  ~classpath:/foo/**/ba?  ");
    AntExpression expression3 = new AntExpression("  /foo/**/ba?  ");
    AntExpression expression4 = new AntExpression("  ~/foo/**/ba?  ");
    AntExpression expression5 = new AntExpression("  **/ba?  ");
    AntExpression expression6 = new AntExpression("  ~**/ba?  ");

    assertThat(expression1.isExclusion(), is(false));
    assertThat(expression2.isExclusion(), is(true));
    assertThat(expression3.isExclusion(), is(false));
    assertThat(expression4.isExclusion(), is(true));
    assertThat(expression5.isExclusion(), is(false));
    assertThat(expression6.isExclusion(), is(true));
  }
}
