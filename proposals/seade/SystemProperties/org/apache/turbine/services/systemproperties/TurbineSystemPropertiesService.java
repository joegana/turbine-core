package org.apache.turbine.services.systemproperties;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.services.InitializationException;
import org.apache.turbine.services.TurbineBaseService;

/**
 * The SystemPropertiesService provides a convenient way of getting properties 
 * defined in TurbineResources.properties into System.properties.
 *
 * <p>Properties defined as:<br/ >
 *     <code>services.SystemPropertiesService.name = value</code><br/ >
 * will be added to System.properties as<br/ >
 *     <code>name=value</code><br/ >
 * Suggested use is to configure mail.host for JavaMail thus:<br/ >
 *     <code>services.SystemPropertiesService.mail.host = localhost</code>
 *
 * @author <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 * @version $Id$
 */
public class TurbineSystemPropertiesService
        extends TurbineBaseService
        implements SystemPropertiesService
{
    private static Log log 
            = LogFactory.getLog(TurbineSystemPropertiesService.class);

    // ---- Service initilization ------------------------------------------

    /**
     * Initializes the service.
     */
    public void init() throws InitializationException
    {
        Configuration conf = getConfiguration();
        Properties systemProperties = System.getProperties();
        for (Iterator iter = conf.getKeys(); iter.hasNext();)
        {
            String key = (String) iter.next();
            if (key.equals("classname") || key.equals("earlyInit"))
            {
                continue;
            }
            if (log.isDebugEnabled())
            {
                log.debug("Setting System property \"" + key + "\" to \"" 
                        + conf.getString(key) + "\"");
            }
            systemProperties.setProperty(key, conf.getString(key));
        }
        
        setInit(true);
    }

}