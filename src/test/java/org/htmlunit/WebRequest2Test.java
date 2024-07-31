/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.util.NameValuePair;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link WebRequest}.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class WebRequest2Test extends WebServerTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getParametersNone() throws Exception {
        final WebRequest request = new WebRequest(URL_FIRST);

        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final WebClient client = getWebClient();

        final Page page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n", ((TextPage) page).getContent());

        final List<NameValuePair> parameters = request.getParameters();
        assertEquals(0, parameters.size());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getParametersOnePair() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final WebClient client = getWebClient();

        final WebRequest request = new WebRequest(new URL(URL_FIRST, "?x=u"));
        Page page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'x': 'u'\n", ((TextPage) page).getContent());

        List<NameValuePair> parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("x", parameters.get(0).getName());
        assertEquals("u", parameters.get(0).getValue());

        final List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("hello", "world"));
        request.setRequestParameters(pairs);
        page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'hello': 'world'\n", ((TextPage) page).getContent());

        parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("hello", parameters.get(0).getName());
        assertEquals("world", parameters.get(0).getValue());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getParametersOnePairKeyEquals() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final WebClient client = getWebClient();

        final WebRequest request = new WebRequest(new URL(URL_FIRST, "?x="));
        Page page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'x': ''\n", ((TextPage) page).getContent());

        List<NameValuePair> parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("x", parameters.get(0).getName());
        assertEquals("", parameters.get(0).getValue());

        final List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("hello", ""));
        request.setRequestParameters(pairs);
        page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'hello': ''\n", ((TextPage) page).getContent());

        parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("hello", parameters.get(0).getName());
        assertEquals("", parameters.get(0).getValue());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getParametersOnePairKeyOnly() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final WebClient client = getWebClient();

        final WebRequest request = new WebRequest(new URL(URL_FIRST, "?x"));
        Page page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'x': ''\n", ((TextPage) page).getContent());

        List<NameValuePair> parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("x", parameters.get(0).getName());
        assertEquals("", parameters.get(0).getValue());

        final List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new NameValuePair("hello", null));
        request.setRequestParameters(pairs);
        page = client.getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'hello': ''\n", ((TextPage) page).getContent());

        parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("hello", parameters.get(0).getName());
        assertEquals("", parameters.get(0).getValue());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getParametersFromQueryAndUrlEncodedBodyPost() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final URL url = new URL(URL_FIRST, "?a=b");
        final WebRequest request = new WebRequest(url);
        request.setHttpMethod(HttpMethod.POST);
        request.setEncodingType(FormEncodingType.URL_ENCODED);
        request.setRequestBody("c=d");

        final Page page = getWebClient().getPage(request);
        assertTrue(page instanceof TextPage);

        assertEquals("Parameters: \n  'a': 'b'\n  'c': 'd'\n", ((TextPage) page).getContent());

        final List<NameValuePair> parameters = request.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("a", parameters.get(0).getName());
        assertEquals("b", parameters.get(0).getValue());
        assertEquals("c", parameters.get(1).getName());
        assertEquals("d", parameters.get(1).getValue());
    }

    @Test
    public void getParametersFromQueryAndUrlEncodedBodyPostWhenEncodingTypeIsMultipart() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final URL url = new URL(URL_FIRST, "?a=b");
        final WebRequest request = new WebRequest(url);
        request.setHttpMethod(HttpMethod.POST);
        request.setEncodingType(FormEncodingType.MULTIPART);
        request.setRequestParameters(Collections.singletonList(new NameValuePair("c", "d")));

        final Page page = getWebClient().getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'a': 'b'\n", ((TextPage) page).getContent());

        final List<NameValuePair> parameters = request.getParameters();
        assertEquals(2, parameters.size());
        assertEquals("a", parameters.get(0).getName());
        assertEquals("b", parameters.get(0).getValue());
        assertEquals("c", parameters.get(1).getName());
        assertEquals("d", parameters.get(1).getValue());
    }

    @Test
    public void getParametersUrlEncodedPostNoBody() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final URL url = new URL(URL_FIRST, "?a=b");
        final WebRequest request = new WebRequest(url);
        request.setHttpMethod(HttpMethod.POST);
        request.setEncodingType(FormEncodingType.URL_ENCODED);

        final Page page = getWebClient().getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'a': 'b'\n", ((TextPage) page).getContent());

        final List<NameValuePair> parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("a", parameters.get(0).getName());
        assertEquals("b", parameters.get(0).getValue());
    }

    @Test
    public void getParametersTextEncodedPostNoBody() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final URL url = new URL(URL_FIRST, "?a=b");
        final WebRequest request = new WebRequest(url);
        request.setHttpMethod(HttpMethod.POST);
        request.setEncodingType(FormEncodingType.TEXT_PLAIN);

        final Page page = getWebClient().getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'a': 'b'\n", ((TextPage) page).getContent());

        final List<NameValuePair> parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("a", parameters.get(0).getName());
        assertEquals("b", parameters.get(0).getValue());
    }

    @Test
    public void getParametersTextEncodedPostBody() throws Exception {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", InspectServlet.class);
        startWebServer("./", null, servlets);

        final URL url = new URL(URL_FIRST, "?a=b");
        final WebRequest request = new WebRequest(url);
        request.setHttpMethod(HttpMethod.POST);
        request.setEncodingType(FormEncodingType.TEXT_PLAIN);
        request.setRequestBody("c=d");

        final Page page = getWebClient().getPage(request);
        assertTrue(page instanceof TextPage);
        assertEquals("Parameters: \n  'a': 'b'\n", ((TextPage) page).getContent());

        final List<NameValuePair> parameters = request.getParameters();
        assertEquals(1, parameters.size());
        assertEquals("a", parameters.get(0).getName());
        assertEquals("b", parameters.get(0).getValue());
    }

    /**
     * Servlet.
     */
    public static class InspectServlet extends HttpServlet {

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        @Override
        protected void doHead(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        @Override
        protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        @Override
        protected void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        @Override
        protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        @Override
        protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        @Override
        protected void doTrace(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
            bounce(req, resp);
        }

        private static void bounce(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
            try (Writer writer = resp.getWriter()) {
                writer.write("Parameters: \n");
                for (final String key : req.getParameterMap().keySet()) {
                    final String val = req.getParameter(key);
                    if (val == null) {
                        writer.write("  '" + key + "': '-null-'\n");
                    }
                    else {
                        writer.write("  '" + key + "': '" + val + "'\n");
                    }
                }
            }
        }
    }
}
