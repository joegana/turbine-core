package org.apache.turbine.services.velocity;


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


import java.io.OutputStream;
import java.io.Writer;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.TurbineServices;
import org.apache.velocity.context.Context;

/**
 * This is a simple static accessor to common Velocity tasks such as
 * getting an instance of a context as well as handling a request for
 * processing a template.
 * <pre>
 * Context context = TurbineVelocity.getContext(data);
 * context.put("message", "Hello from Turbine!");
 * String results = TurbineVelocity.handleRequest(context, "helloWorld.vm");
 * data.getPage().getBody().addElement(results);
 * </pre>
 *
 * @author <a href="mailto:john.mcnally@clearink.com">John D. McNally</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:jvanzyl@periapt.com.com">Jason van Zyl</a>
 * @author <a href="mailto:peter@courcoux.biz">Peter Courcoux</a>
 * @version $Id$
 */
public abstract class TurbineVelocity
{
    /**
     * Utility method for accessing the service
     * implementation
     *
     * @return a VelocityService implementation instance
     */
    public static VelocityService getService()
    {
        return (VelocityService) TurbineServices
                .getInstance().getService(VelocityService.SERVICE_NAME);
    }

    /**
     * This allows you to pass in a context and a path to a template
     * file and then grabs an instance of the velocity service and
     * processes the template and returns the results as a String
     * object.
     *
     * @param context A Context.
     * @param template The path for the template files.
     * @return A String.
     * @exception Exception a generic exception.
     */
    public static String handleRequest(Context context, String template)
            throws Exception
    {
        return getService().handleRequest(context, template);
    }

    /**
     * Process the request and fill in the template with the values
     * you set in the Context.
     *
     * @param context A Context.
     * @param template A String with the filename of the template.
     * @param out A OutputStream where we will write the process template as
     * a String.
     * @exception Exception a generic exception.
     */
    public static void handleRequest(Context context, String template,
                                     OutputStream out)
            throws Exception
    {
        getService().handleRequest(context, template, out);
    }

    /**
     * Process the request and fill in the template with the values
     * you set in the Context.
     *
     * @param context A Context.
     * @param template A String with the filename of the template.
     * @param writer A Writer where we will write the process template as
     * a String.
     * @exception Exception a generic exception.
     */
    public static void handleRequest(Context context,
                                     String template,
                                     Writer writer)
            throws Exception
    {
        getService().handleRequest(context, template, writer);
    }

    /**
     * This returns a Context that you can pass into handleRequest
     * once you have populated it with information that the template
     * will know about.
     *
     * @param pipelineData A Turbine PipelineData.
     * @return A Context.
     */
    public static Context getContext(PipelineData pipelineData)
    {
        return getService().getContext(pipelineData);
    }

    /**
     * This method returns a blank Context object, which
     * also contains the global context object. Do not use
     * this method if you need an empty context object! Use
     * getNewContext for this.
     *
     * @return A WebContext.
     */
    public static Context getContext()
    {
        return getService().getContext();
    }

    /**
     * This method returns a new, empty Context object.
     *
     * @return A WebContext.
     */
    public static Context getNewContext()
    {
        return getService().getNewContext();
    }

    /**
     * Performs post-request actions (releases context
     * tools back to the object pool).
     *
     * @param context a Velocity Context
     */
    public static void requestFinished(Context context)
    {
        getService().requestFinished(context);
    }
}
