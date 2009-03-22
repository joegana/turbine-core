package org.apache.turbine.pipeline;


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


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;

/**
 * Implements the Redirect Requested portion of the "Turbine classic"
 * processing pipeline (from the Turbine 2.x series).
 *
 * @author <a href="mailto:epugh@opensourceConnections.com">Eric Pugh</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public class DetermineRedirectRequestedValve
    extends AbstractValve
{
    private static final Log log = LogFactory.getLog(DetermineRedirectRequestedValve.class);
    /**
     * Creates a new instance.
     */
    public DetermineRedirectRequestedValve()
    {
        // empty constructor
    }

    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(PipelineData pipelineData, ValveContext context)
        throws IOException, TurbineException
    {
        try
        {
            redirectRequested(pipelineData);
        }
        catch (Exception e)
        {
            throw new TurbineException(e);
        }

        // Pass control to the next Valve in the Pipeline
        context.invokeNext(pipelineData);
    }

    /**
     * Perform clean up after processing the request.
     *
     * @param data The run-time data.
     */
    protected void redirectRequested(PipelineData pipelineData)
        throws Exception
    {
        RunData data = getRunData(pipelineData);
        // handle a redirect request
        boolean requestRedirected = ((data.getRedirectURI() != null)
        && (data.getRedirectURI().length() > 0));
        if (requestRedirected)
        {
            if (data.getResponse().isCommitted())
            {
                requestRedirected = false;
                log.warn("redirect requested, response already committed: " +
                        data.getRedirectURI());
            }
            else
            {
                data.getResponse().sendRedirect(data.getRedirectURI());
            }
        }

        if (!requestRedirected)
        {
            try
            {
                if (data.isPageSet() == false && data.isOutSet() == false)
                {
                    throw new Exception("Nothing to output");
                }

                // We are all done! if isPageSet() output that way
                // otherwise, data.getOut() has already been written
                // to the data.getOut().close() happens below in the
                // finally.
                if (data.isPageSet() && data.isOutSet() == false)
                {
                    // Modules can override these.
                    data.getResponse().setLocale(data.getLocale());
                    data.getResponse().setContentType(
                            data.getContentType());

                    // Set the status code.
                    data.getResponse().setStatus(data.getStatusCode());
                    // Output the Page.
                    data.getPage().output(data.getResponse().getWriter());
                }
            }
            catch (Exception e)
            {
                // The output stream was probably closed by the client
                // end of things ie: the client clicked the Stop
                // button on the browser, so ignore any errors that
                // result.
                log.debug("Output stream closed? ", e);
            }
        }
    }
}
