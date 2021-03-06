package org.apache.turbine.util.uri;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.Part;

import org.apache.fulcrum.parser.DefaultParameterParser;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.parser.ParserService;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.test.BaseTestCase;
import org.apache.turbine.util.ServerData;
import org.apache.turbine.util.TurbineConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Testing of the TurbineURI class
 *
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
public class TurbineURITest extends BaseTestCase
{
    private TurbineURI turi;

    private ParserService parserService;

    private static TurbineConfig tc = null;


    @BeforeClass
    public static void init() {
        tc =  new TurbineConfig(
                            ".",
                            "/conf/test/CompleteTurbineResources.properties");
        tc.initialize();
    }
    /**
     * Performs any initialization that must happen before each test is run.
     */

    @Before
    public void setup()
    {
        // Setup configuration

        ServerData sd = new ServerData("www.testserver.com",
                URIConstants.HTTP_PORT, URIConstants.HTTP, "/servlet/turbine",
                "/context");
        turi = new TurbineURI(sd);

        parserService = (ParserService)TurbineServices.getInstance().getService(ParserService.ROLE);
    }

    /**
     * Clean up after each test is run.
     */
    @AfterClass
    public static void tearDown()
    {
        if (tc != null)
        {
            tc.dispose();
        }

    }

    @Test public void testAddRemove()
    {

        assertFalse("TurbineURI should not have a pathInfo", turi.hasPathInfo());
        assertFalse("TurbineURI must not have a queryData", turi.hasQueryData());
        turi.addPathInfo("test", "x");
        assertTrue("TurbineURI must have a pathInfo", turi.hasPathInfo());
        assertFalse("TurbineURI must not have a queryData", turi.hasQueryData());
        turi.removePathInfo("test");
        assertFalse("TurbineURI must not have a pathInfo", turi.hasPathInfo());
        assertFalse("TurbineURI must not have a queryData", turi.hasQueryData());

        turi.addQueryData("test", "x");
        assertTrue("TurbineURI must have a queryData", turi.hasQueryData());
        assertFalse("TurbineURI must not have a pathInfo", turi.hasPathInfo());
        turi.removeQueryData("test");
        assertFalse("TurbineURI must not have a queryData", turi.hasQueryData());
        assertFalse("TurbineURI must not have a pathInfo", turi.hasPathInfo());
    }

    @Test public void testEmptyAndNullQueryData()
    {
        // Check empty String
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
        turi.addQueryData("test", "");
        assertEquals("/context/servlet/turbine?test=", turi.getRelativeLink());
        turi.removeQueryData("test");

        // Check null
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
        turi.addQueryData("test", null);
        assertEquals("/context/servlet/turbine?test=null", turi
                .getRelativeLink());
        turi.removeQueryData("test");
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
    }

    @Test public void testEmptyAndNullPathInfo()
    {
        // Check empty String
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
        turi.addPathInfo("test", "");
        // Kind of susspect result - might result in "//" in the URL.
        assertEquals("/context/servlet/turbine/test/", turi.getRelativeLink());
        turi.removePathInfo("test");

        // Check null
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
        turi.addPathInfo("test", null);
        assertEquals("/context/servlet/turbine/test/null", turi
                .getRelativeLink());
        turi.removePathInfo("test");
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
    }

    @Test public void testAddEmptyParameterParser()
    {
        ParameterParser pp = new DefaultParameterParser();
        turi.add(1, pp); // 1 = query data
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
    }

    @Test public void testAddParameterParser() throws InstantiationException
    {
        ParameterParser pp = parserService.getParser(DefaultParameterParser.class);
        pp.add("test", "");
        turi.add(1, pp); // 1 = query data
        assertEquals("/context/servlet/turbine?test=", turi.getRelativeLink());
        turi.removeQueryData("test");
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());

        parserService.putParser(pp);
        pp = parserService.getParser(DefaultParameterParser.class);
        pp.add("test", "null");
//        pp.add("test", (String) null); // isnotnull guarded
        turi.add(1, pp); // 1 = query data
        // Should make the following work so as to be consistent with directly
        // added values.
        assertEquals("/context/servlet/turbine?test=null",
        turi.getRelativeLink());

        turi.removeQueryData("test");
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());

        parserService.putParser(pp);
        pp = parserService.getParser(DefaultParameterParser.class);
        pp.add("test", "1");
        pp.add("test", "2");
        turi.add(1, pp); // 1 = query data
        assertEquals("/context/servlet/turbine?test=1&test=2", turi.getRelativeLink());
        turi.removeQueryData("test");
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());

        parserService.putParser(pp);
        pp = parserService.getParser(DefaultParameterParser.class);
        Part part = Mockito.mock(Part.class);
        pp.add("upload-field", part);
        turi.add(1, pp); // 1 = query data
        assertEquals("/context/servlet/turbine?upload-field=", turi.getRelativeLink());
        turi.removeQueryData("upload-field");
        assertEquals("/context/servlet/turbine", turi.getRelativeLink());
    }

}
