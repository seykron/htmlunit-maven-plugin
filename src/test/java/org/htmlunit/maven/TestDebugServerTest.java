package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.htmlunit.NanoHTTPD.Method;
import org.htmlunit.NanoHTTPD.Response;
import org.htmlunit.NanoHTTPD.Response.Status;
import org.junit.Test;

/** Tests the {@link TestDebugServer} class.
 */
public class TestDebugServerTest {

  @Test
  public void getStaticContentUrl() throws MalformedURLException {
    URL url = new URL("classpath:/foo/bar.js");
    URL debugUrl = TestDebugServer.getStaticContentUrl("foo.com", 1234, url);
    assertThat(debugUrl.toString(), is("http://foo.com:1234/static/?url="
        + "classpath:/foo/bar.js"));
  }

  @Test
  public void getDebugBootstrapScripts() throws MalformedURLException {
    List<URL> debugUrl = TestDebugServer.getDebugBootstrapScripts("foo.com",
        1234);
    assertThat(debugUrl.size(), is(1));
    assertThat(debugUrl.get(0).toString()
        .endsWith("classpath:/org/htmlunit/maven/TestDebug.js"), is(true));
  }

  @Test
  public void handleDefault() throws MalformedURLException {
    URL url = new URL("classpath:/foo/bar.js");
    TestDebugServer server = new TestDebugServer(1234, Arrays.asList(url)) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }
    };
    Response response = server.handleDefault(new HashMap<String, String>());
    assertThat(readResponse(response).startsWith("/test/" + url.toString()),
        is(true));
  }

  @Test
  public void handleStatic() throws MalformedURLException {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }
    };
    Map<String, String> params = new HashMap<String, String>();
    params.put("url", "classpath:/org/htmlunit/maven/BarWidget.js");
    Response response = server.handleStatic("/static",
        params);
    assertThat(readResponse(response).contains("PROP_BAR"),
        is(true));
  }

  @Test
  public void handleStatic_notFound() throws MalformedURLException {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }
    };
    Map<String, String> params = new HashMap<String, String>();
    params.put("url", "classpath:/not/found.js");
    Response response = server.handleStatic("/static",
        params);
    assertThat(response.status, is(Status.NOT_FOUND));
  }

  @Test
  public void handleStatic_debug() throws MalformedURLException {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }

      @Override
      protected InputStream getDebugScript() {
        String debugData = "DEBUG_DATA";
        return new ByteArrayInputStream(debugData.getBytes());
      }
    };
    Map<String, String> params = new HashMap<String, String>();
    params.put("url", "classpath:/org/htmlunit/maven/TestDebug.js");
    Response response = server.handleStatic("/static",
        params);
    assertThat(readResponse(response).contains("DEBUG_DATA"),
        is(true));
  }

  @Test
  public void handleTest() throws MalformedURLException {
    URL url = new URL("classpath:/foo/bar.js");
    TestDebugServer server = new TestDebugServer(1234, Arrays.asList(url)) {
      @Override
      protected URL getRunner(final URL testFile) {
        try {
          return new URL("classpath:/org/htmlunit/maven/BarWidget.js");
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    };
    Response response = server.handleTest("/test/classpath:/foo/bar.js",
        new HashMap<String, String>());
    assertThat(readResponse(response).contains("BarWidget"),
        is(true));
  }

  @Test
  public void handleTest_notFound() throws MalformedURLException {
    URL url = new URL("classpath:/foo/bar.js");
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        try {
          return new URL("classpath:/org/htmlunit/maven/BarWidget.js");
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    };
    server.setTestFiles(Arrays.asList(url));
    Response response = server.handleTest("/test/classpath:/not/found.js",
        new HashMap<String, String>());
    assertThat(response.status, is(Status.NOT_FOUND));
  }

  @Test
  public void handleDisconnect() throws MalformedURLException {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }
    };
    Response response = server.handleDisconnect("/disconnect",
        new HashMap<String, String>());
    assertThat(readResponse(response), is("Bye"));
  }

  @Test
  public void serve() {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }

      @SuppressWarnings("unused")
      public Response handleFoo(final String uri,
          final Map<String, String> params) {
        assertThat(uri, is("/foo/"));
        return new Response("bar");
      }
    };

    Response response = server.serve("/foo/", Method.GET,
        new HashMap<String, String>(), new HashMap<String, String>(),
        new HashMap<String, String>());

    assertThat(readResponse(response), is("bar"));
  }

  @Test
  public void serve_default() {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }
      @Override
      public Response handleDefault(final Map<String, String> params) {
        return new Response("bar");
      }
    };

    Response response = server.serve("/", Method.GET,
        new HashMap<String, String>(), new HashMap<String, String>(),
        new HashMap<String, String>());

    assertThat(readResponse(response), is("bar"));
  }

  @Test
  public void serve_notFound() {
    TestDebugServer server = new TestDebugServer(1234, new ArrayList<URL>()) {
      @Override
      protected URL getRunner(final URL testFile) {
        return null;
      }
    };

    Response response = server.serve("/asdf", Method.GET,
        new HashMap<String, String>(), new HashMap<String, String>(),
        new HashMap<String, String>());

    assertThat(response.status, is(Status.NOT_FOUND));
  }

  private String readResponse(final Response response) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      IOUtils.copy(response.data, out);
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(response.data);
    }
  }
}
