package org.apache.turbine.modules.layouts;


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


import java.io.StringReader;

import org.apache.fulcrum.xslt.XSLTService;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineLoader;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.modules.Screen;
import org.apache.turbine.modules.ScreenLoader;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * This Layout module allows Velocity XML templates to be used as layouts.
 * <br><br>
 * Once the (XML) screen and navigation templates have been inserted into
 * the layout template the result is transformed with a XSL stylesheet.
 * The stylesheet (with the same name than the screen template) is loaded
 * and executed by the XSLT service, so it is important that you correctly
 * set up your XSLT service.  If the named stylsheet does not exist the
 * default.xsl stylesheet is executed.  If default.xsl does not exist
 * the XML is merely echoed.
 * <br><br>
 * Since dynamic content is supposed to be primarily located in
 * screens and navigations there should be relatively few reasons to
 * subclass this Layout.
 *
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class VelocityXslLayout extends VelocityOnlyLayout
{
    /** Injected service instance */
    @TurbineService
    private XSLTService xsltService;

    /** Injected loader instance */
    @TurbineLoader( Screen.class )
    private ScreenLoader screenLoader;

    /**
     * Build the layout.  Also sets the ContentType and Locale headers
     * of the HttpServletResponse object.
     *
     * @param pipelineData Turbine information.
     * @throws Exception a generic exception.
     */
    @Override
    public void doBuild(PipelineData pipelineData)
        throws Exception
    {
        RunData data = pipelineData.getRunData();
        // Get the context needed by Velocity.
        Context context = velocityService.getContext(pipelineData);

        data.getResponse().setContentType(TurbineConstants.DEFAULT_HTML_CONTENT_TYPE);

        // Provide objects to Velocity context
        populateContext(pipelineData, context);

        // Grab the layout template set in the VelocityPage.
        // If null, then use the default layout template
        // (done by the TemplateInfo object)
        String templateName = data.getTemplateInfo().getLayoutTemplate();

        log.debug("Now trying to render layout {}", templateName);

        // Now, generate the layout template.
        String temp = velocityService.handleRequest(context,
                prefix + templateName);

        // Finally we do a transformation and send the result
        // back to the browser
        xsltService.transform(
            data.getTemplateInfo().getScreenTemplate(),
                new StringReader(temp), data.getResponse().getWriter());
    }
}
