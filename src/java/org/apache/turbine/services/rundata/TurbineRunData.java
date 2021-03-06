package org.apache.turbine.services.rundata;

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

import org.apache.fulcrum.parser.CookieParser;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.pool.Recyclable;
import org.apache.turbine.util.RunData;

/**
 * TurbineRunData is an extension to the RunData interface to be
 * implemented by RunData implementations to be distributed by
 * the Turbine RunData Service. The extensions define methods
 * that are used by the service for initializing the implementation,
 * but which are not meant to be called by the actual client objects.
 *
 * <p>TurbineRunData extends also the Recyclable interface making
 * it possible to pool its implementations for recycling.
 *
 * @author <a href="mailto:ilkka.priha@simsoft.fi">Ilkka Priha</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:bhoeneis@ee.ethz.ch">Bernie Hoeneisen</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public interface TurbineRunData
    extends RunData,
            Recyclable
{
    /**
     * Gets the parameter parser without parsing the parameters.
     *
     * @return the parameter parser.
     */
    ParameterParser getParameterParser();

    /**
     * Gets the cookie parser without parsing the cookies.
     *
     * @return the cookie parser.
     */
    CookieParser getCookieParser();
}
