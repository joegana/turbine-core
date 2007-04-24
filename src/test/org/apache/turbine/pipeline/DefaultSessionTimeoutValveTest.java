package org.apache.turbine.pipeline;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletResponse;

import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.template.TemplateService;
import org.apache.turbine.test.BaseTestCase;
import org.apache.turbine.test.EnhancedMockHttpServletRequest;
import org.apache.turbine.test.EnhancedMockHttpSession;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineConfig;
import org.apache.turbine.util.uri.URIConstants;

import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockServletConfig;

/**
 * Tests TurbinePipeline.
 *
 * @author <a href="mailto:epugh@opensourceConnections.com">Eric Pugh</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public class DefaultSessionTimeoutValveTest extends BaseTestCase
{
    private static TurbineConfig tc = null;
    private static TemplateService ts = null;
    private MockServletConfig config = null;
    private EnhancedMockHttpServletRequest request = null;
    private EnhancedMockHttpSession session = null;
    private HttpServletResponse response = null;
    private static ServletConfig sc = null;
    /**
     * Constructor
     */
    public DefaultSessionTimeoutValveTest(String testName) throws Exception
    {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        config = new MockServletConfig();
        config.setupNoParameters();
        request = new EnhancedMockHttpServletRequest();
        request.setupServerName("bob");
        request.setupGetProtocol("http");
        request.setupScheme("scheme");
        request.setupPathInfo("damn");
        request.setupGetServletPath("damn2");
        request.setupGetContextPath("wow");
        request.setupGetContentType("html/text");
        request.setupAddHeader("Content-type", "html/text");
        request.setupAddHeader("Accept-Language", "en-US");
        
       
        
       
        
        
        session = new EnhancedMockHttpSession();
        response = new MockHttpServletResponse();
        
        session.setupGetAttribute(User.SESSION_KEY, null);
       
        request.setSession(session);
        
        
        
        sc = config;
        tc =
            new TurbineConfig(
                    ".",
            "/conf/test/CompleteTurbineResources.properties");
        tc.initialize();
    }
    
    /**
     * Tests the Valve.
     */
    public void testDefaults() throws Exception
    {
        Vector v = new Vector();
        v.add(URIConstants.CGI_ACTION_PARAM);
        request.setupGetParameterNames(v.elements());
        
        request.setupAddParameter(URIConstants.CGI_ACTION_PARAM,"TestAction");
        
        
        PipelineData pipelineData = getPipelineData(request,response,config);
        
        Pipeline pipeline = new TurbinePipeline();

        DefaultSessionTimeoutValve valve = new DefaultSessionTimeoutValve();
        pipeline.addValve(valve);

        pipeline.invoke(pipelineData);

        RunData runData = (RunData)pipelineData;
        assertEquals(0,runData.getSession().getMaxInactiveInterval());


    }
    
    /**
     * Tests the Valve.
     */
    public void testTimeoutSet() throws Exception
    {
        
        Turbine.getConfiguration().setProperty(TurbineConstants.SESSION_TIMEOUT_KEY,"3600");
        PipelineData pipelineData = getPipelineData(request,response,config);
        
        Pipeline pipeline = new TurbinePipeline();

        DefaultSessionTimeoutValve valve = new DefaultSessionTimeoutValve();
        pipeline.addValve(valve);

        pipeline.invoke(pipelineData);
        RunData runData = (RunData)pipelineData;

        assertEquals(3600,runData.getSession().getMaxInactiveInterval());


    }    
  
    
   
}