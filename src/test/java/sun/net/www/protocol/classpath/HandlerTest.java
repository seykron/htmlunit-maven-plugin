package sun.net.www.protocol.classpath;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/** Tests the {@link Handler} class.
 */
public class HandlerTest {

  @Test
  public void readUrl() throws Exception {
    URL url = new URL("classpath:/org/htmlunit/maven/DefaultTestRunner.html");
    InputStream input = url.openStream();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    IOUtils.copy(input, output);
    assertThat(output.toString().contains("$testRunnerScript$"), is(true));
  }

  @Test(expected = IOException.class)
  public void readUrl_notFound() throws Exception {
    URL url = new URL("classpath:/not/found.js");
    url.openStream();
  }
}
