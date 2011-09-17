/**
 * Copyright © 2010-2011 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.restdriver.clientdriver.unit;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import com.github.restdriver.clientdriver.ClientDriverRequest;
import com.github.restdriver.clientdriver.ClientDriverRequest.Method;
import com.github.restdriver.clientdriver.DefaultRequestMatcher;

// suppressed to allow inline definition of maps with asMap()
@SuppressWarnings("unchecked")
public class DefaultRequestMatcherTest {

    private DefaultRequestMatcher sut;
    private HttpServletRequest aReq;

    @Before
    public void before() {
        sut = new DefaultRequestMatcher();
    }

    @Test
    public void testMatchNoParams() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET);
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        assertThat(sut.isMatch(aReq, bReq), is(true));
    }

    @Test
    public void testMatchNoParamsPattern() {

        ClientDriverRequest bReq = new ClientDriverRequest(Pattern.compile("[a]{5}")).withMethod(Method.GET);
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWithParams() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa")
                                        .withMethod(Method.GET)
                                        .withParam("kk", "vv")
                                        .withParam("k2", "v2");

        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("vv"), "k2", asStringArray("v2")));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWithParamsPattern() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk",
                Pattern.compile("[v]{2}")).withParam("k2", Pattern.compile("v[0-9]"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("vv"), "k2", asStringArray("v2")));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWithWrongParam() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk", "vv");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("not vv")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithWrongParamPattern() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk",
                Pattern.compile("[v]{2}"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("xx")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithNullParam() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk", "vv");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray((String[]) null)));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithParamsTooMany() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa")
                                        .withMethod(Method.GET)
                                        .withParam("kk", "vv")
                                        .withParam("k2", "v2");

        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("k1", asStringArray("v1")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithParamsTooFew() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk", "vv");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("k1", asStringArray("v1"), "k2", asStringArray("v2")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testSuccessfulMatchWithMultipleIdenticalParams() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk", "vv").withParam("kk", "vvv");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("vv", "vvv")));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testSuccessfulMatchWithMultipleIdenticalParamsInDifferentOrder() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("key", "this").withParam("key", "that");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("key", asStringArray("that", "this")));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testFailedMatchWithMultipleIdenticalParams() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk", "vv").withParam("kk", "vvv");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("vv", "v2")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testFailedMatchWithMultipleIdenticalParamsInDifferentOrder() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("key", "this").withParam("key", "tha");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("key", asStringArray("that", "this")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testFailedMatchWithWrongNumberOfIdenticalParams() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("key", "this").withParam("key", "that");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("key", asStringArray("that")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testFailedMatchWithOneWrongParamPattern() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withParam("kk", Pattern.compile("[v]{2}"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap("kk", asStringArray("v1", "v2")));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWrongMethod() {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.DELETE);
        aReq = mock(HttpServletRequest.class);

        when(aReq.getMethod()).thenReturn("GET");

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWrongPath() {

        ClientDriverRequest bReq = new ClientDriverRequest("bbbbb").withMethod(Method.GET);
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWrongPathPattern() {

        ClientDriverRequest bReq = new ClientDriverRequest(Pattern.compile("[b]{5}")).withMethod(Method.GET);
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithRequestBody() throws IOException {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withBody("ooooh", "text/junk");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getContentType()).thenReturn("text/junk");

        BufferedReader contentReader = new BufferedReader(new StringReader("ooooh"));
        when(aReq.getReader()).thenReturn(contentReader);

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWithRequestBodyPattern() throws IOException {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withBody(Pattern.compile("[o]{4}h"),
                Pattern.compile("text/j[a-z]{3}"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getContentType()).thenReturn("text/junk");

        BufferedReader contentReader = new BufferedReader(new StringReader("ooooh"));
        when(aReq.getReader()).thenReturn(contentReader);

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWithRequestBodyWrongType() throws IOException {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withBody("ooooh", "text/junk");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getContentType()).thenReturn("text/jnkular");

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithRequestBodyWrongTypePattern() throws IOException {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withBody("ooooh",
                Pattern.compile("text/[a-z]{4}"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getContentType()).thenReturn("text/jnkular");

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithRequestBodyWrongContent() throws IOException {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withBody("ooooh", "text/junk");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getContentType()).thenReturn("text/junk");

        BufferedReader contentReader = new BufferedReader(new StringReader("ooook"));
        when(aReq.getReader()).thenReturn(contentReader);

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithRequestBodyWrongContentPattern() throws IOException {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withBody(Pattern.compile("[o]{4}h"),
                "text/junk");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getContentType()).thenReturn("text/junk");

        BufferedReader contentReader = new BufferedReader(new StringReader("ooook"));
        when(aReq.getReader()).thenReturn(contentReader);

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithRequestHeaderString() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Cache-Control", "no-cache");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Cache-Control")).thenReturn(enumerationFrom("no-cache"));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchMultipleWithRequestHeaderString() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Some-Header", "bar");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Some-Header")).thenReturn(enumerationFrom("foo", "bar"));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWrongWithRequestHeaderString() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Cache-Control", "no-cache");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Cache-Control")).thenReturn(enumerationFrom("cache-please!"));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWrongWithMissingRequestHeaderString() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Cache-Control", "no-cache");
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Cache-Control")).thenReturn(null);

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWithRequestHeaderPattern() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Content-Length", Pattern.compile("\\d+"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Content-Length")).thenReturn(enumerationFrom("1234"));

        assertThat(sut.isMatch(aReq, bReq), is(true));

    }

    @Test
    public void testMatchWrongWithRequestHeaderPattern() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Content-Length", Pattern.compile("\\d+"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Content-Length")).thenReturn(enumerationFrom("invalid"));

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    @Test
    public void testMatchWrongWithMissingRequestHeaderPattern() throws Exception {

        ClientDriverRequest bReq = new ClientDriverRequest("aaaaa").withMethod(Method.GET).withHeader("Content-Length", Pattern.compile("\\d+"));
        aReq = mock(HttpServletRequest.class);

        when(aReq.getPathInfo()).thenReturn("aaaaa");
        when(aReq.getMethod()).thenReturn("GET");
        when(aReq.getParameterMap()).thenReturn(asMap());

        when(aReq.getHeaders("Content-Length")).thenReturn(null);

        assertThat(sut.isMatch(aReq, bReq), is(false));

    }

    private <T> Enumeration<T> enumerationFrom(T... items) {
        return Collections.enumeration(Arrays.asList(items));
    }

    private String[] asStringArray(String... strings) {
        return strings;
    }

    @SuppressWarnings("rawtypes")
    private static Map asMap(Object... objects) {
        Map map = new HashMap();

        if (objects.length % 2 != 0) {
            throw new RuntimeException("There should be an even number of objects given");
        }

        Object previous = null;

        for (Object object : objects) {
            if (previous == null) {
                previous = object;
            } else {
                map.put(previous, object);
                previous = null;
            }
        }

        return map;
    }

}
