package org.apache.turbine.services.template.mapper;


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


import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.services.template.TemplateService;

/**
 * The most primitive templating mapper. It is used for the navigation template
 * objects. It never caches and simply returns what is given to it but keeps
 * the template extension.
 *
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class DirectTemplateMapper
    extends BaseTemplateMapper
    implements Mapper
{
    /**
     * Default C'tor. If you use this C'tor, you must use
     * the bean setter to set the various properties needed for
     * this mapper before first usage.
     */
    public DirectTemplateMapper()
    {
        super();
    }

    /**
     * Replace all "," with ".", but keep the extension.
     *
     * about,directions,Driving.vm --> about/directions/Driving.vm
     *
     * @param template The template name.
     * @return A class name for the given template.
     */
    @Override
    public String doMapping(String template)
    {
        String [] components
            = StringUtils.split(template, String.valueOf(TemplateService.TEMPLATE_PARTS_SEPARATOR));

        return StringUtils.join(components, String.valueOf(separator));
    }
}
