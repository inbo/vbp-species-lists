package au.org.ala.listsapi.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LegacyRedirectFilterTest {

  private final LegacyRedirectFilter filter =
      new LegacyRedirectFilter("/v2", "/upload", "/ingest", "/delete", "/constraints");

  private MockHttpServletResponse run(String contextPath, String path, String query)
      throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", contextPath + path);
    request.setContextPath(contextPath);
    request.setQueryString(query);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    filter.doFilter(request, response, chain);
    return response;
  }

  @Test
  void downloadRedirectKeepsContextPath() throws Exception {
    MockHttpServletResponse response =
        run("/api/lists", "/speciesListItem/downloadList/dr565", "fetch=x");
    assertEquals(301, response.getStatus());
    assertEquals("/api/lists/v2/download/dr565", response.getHeader("Location"));
  }

  @Test
  void downloadRedirectWithoutContextPath() throws Exception {
    MockHttpServletResponse response = run("", "/speciesListItem/downloadList/dr565", null);
    assertEquals("/v2/download/dr565", response.getHeader("Location"));
  }

  @Test
  void preVersionRedirectKeepsContextPathAndQuery() throws Exception {
    MockHttpServletResponse response = run("/api/lists", "/ingest/dr1", "dryRun=true");
    assertEquals(301, response.getStatus());
    assertEquals("/api/lists/v2/ingest/dr1?dryRun=true", response.getHeader("Location"));
  }

  @Test
  void preVersionRedirectWithoutContextPath() throws Exception {
    MockHttpServletResponse response = run("", "/upload", null);
    assertEquals("/v2/upload", response.getHeader("Location"));
  }

  @Test
  void otherPathsPassThrough() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/lists/v2/upload");
    request.setContextPath("/api/lists");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    filter.doFilter(request, response, chain);
    assertNull(response.getHeader("Location"));
    assertNotNull(chain.getRequest());
  }
}
