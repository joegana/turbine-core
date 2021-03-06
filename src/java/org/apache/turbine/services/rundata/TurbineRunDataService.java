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

import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.Configuration;
import org.apache.fulcrum.parser.CookieParser;
import org.apache.fulcrum.parser.DefaultCookieParser;
import org.apache.fulcrum.parser.DefaultParameterParser;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.parser.ParserService;
import org.apache.fulcrum.pool.PoolException;
import org.apache.fulcrum.pool.PoolService;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.InitializationException;
import org.apache.turbine.services.TurbineBaseService;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServerData;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.util.TurbineRuntimeException;

/**
 * The RunData Service provides the implementations for RunData and
 * related interfaces required by request processing. It supports
 * different configurations of implementations, which can be selected
 * by specifying a configuration key. It may use pooling, in which case
 * the implementations should implement the Recyclable interface.
 *
 * @author <a href="mailto:ilkka.priha@simsoft.fi">Ilkka Priha</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public class TurbineRunDataService
    extends TurbineBaseService
    implements RunDataService
{

    /** The default implementation of the RunData object*/
    private static final String DEFAULT_RUN_DATA =
        DefaultTurbineRunData.class.getName();

    /** The default implementation of the Parameter Parser object */
    private static final String DEFAULT_PARAMETER_PARSER =
        DefaultParameterParser.class.getName();

    /** The default implementation of the Cookie parser object */
    private static final String DEFAULT_COOKIE_PARSER =
        DefaultCookieParser.class.getName();

    /** The map of configurations. */
    private final ConcurrentMap<String, Object> configurations = new ConcurrentHashMap<>();

    /** A class cache. */
    private final ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<>();

    /** Private reference to the pool service for object recycling */
    private PoolService pool = null;

    /** Private reference to the parser service for parser recycling */
    private ParserService parserService = null;

    /**
     * Constructs a RunData Service.
     */
    public TurbineRunDataService()
    {
        super();
    }

    /**
     * Initializes the service by setting the pool capacity.
     *
     * @throws InitializationException if initialization fails.
     */
    @Override
    public void init()
            throws InitializationException
    {
        // Create a default configuration.
        String[] def = new String[]
        {
            DEFAULT_RUN_DATA,
            DEFAULT_PARAMETER_PARSER,
            DEFAULT_COOKIE_PARSER
        };
        configurations.put(DEFAULT_CONFIG, def.clone());

        // Check other configurations.
        Configuration conf = getConfiguration();
        if (conf != null)
        {
            String key,value;
            String[] config;
            String[] plist = new String[]
            {
                RUN_DATA_KEY,
                PARAMETER_PARSER_KEY,
                COOKIE_PARSER_KEY
            };
            for (Iterator<String> i = conf.getKeys(); i.hasNext();)
            {
                key = i.next();
                value = conf.getString(key);
                int j = 0;
                for (String plistKey : plist)
                {
                    if (key.endsWith(plistKey) && key.length() > plistKey.length() + 1)
                    {
                        key = key.substring(0, key.length() - plistKey.length() - 1);
                        config = (String[]) configurations.get(key);
                        if (config == null)
                        {
                            config = def.clone();
                            configurations.put(key, config);
                        }
                        config[j] = value;
                        break;
                    }
                    j++;
                }
            }
        }

		pool = (PoolService)TurbineServices.getInstance().getService(PoolService.ROLE);

        if (pool == null)
        {
            throw new InitializationException("RunData Service requires"
                + " configured Pool Service!");
        }

        parserService = (ParserService)TurbineServices.getInstance().getService(ParserService.ROLE);

        if (parserService == null)
        {
            throw new InitializationException("RunData Service requires"
                + " configured Parser Service!");
        }

        setInit(true);
    }

    /**
     * Shutdown the service
     *
     * @see org.apache.turbine.services.TurbineBaseService#shutdown()
     */
    @Override
    public void shutdown()
    {
        classCache.clear();
        super.shutdown();
    }

    /**
     * Gets a default RunData object.
     *
     * @param req a servlet request.
     * @param res a servlet response.
     * @param config a servlet config.
     * @return a new or recycled RunData object.
     * @throws TurbineException if the operation fails.
     */
    @Override
    public RunData getRunData(HttpServletRequest req,
                              HttpServletResponse res,
                              ServletConfig config)
            throws TurbineException
    {
        return getRunData(DEFAULT_CONFIG, req, res, config);
    }

    /**
     * Gets a RunData instance from a specific configuration.
     *
     * @param key a configuration key.
     * @param req a servlet request.
     * @param res a servlet response.
     * @param config a servlet config.
     * @return a new or recycled RunData object.
     * @throws TurbineException if the operation fails.
     * @throws IllegalArgumentException if any of the parameters are null.
     * TODO The "key" parameter should be removed in favor of just looking up what class via the roleConfig avalon file.
     */
    @Override
    public RunData getRunData(String key,
                              HttpServletRequest req,
                              HttpServletResponse res,
                              ServletConfig config)
            throws TurbineException,
            IllegalArgumentException
    {
        // The RunData object caches all the information that is needed for
        // the execution lifetime of a single request. A RunData object
        // is created/recycled for each and every request and is passed
        // to each and every module. Since each thread has its own RunData
        // object, it is not necessary to perform synchronization for
        // the data within this object.
        if (req == null || res == null || config == null)
        {
            throw new IllegalArgumentException("HttpServletRequest, "
                + "HttpServletResponse or ServletConfig was null.");
        }

        // Get the specified configuration.
        String[] cfg = (String[]) configurations.get(key);
        if (cfg == null)
        {
            throw new TurbineException("RunTime configuration '" + key + "' is undefined");
        }

        TurbineRunData data;
        try
        {
    		Class<?> runDataClazz = classCache.computeIfAbsent(cfg[0], this::classForName);
            Class<?> parameterParserClazz = classCache.computeIfAbsent(cfg[1], this::classForName);
            Class<?> cookieParserClazz = classCache.computeIfAbsent(cfg[2], this::classForName);

            data = (TurbineRunData) pool.getInstance(runDataClazz);
            @SuppressWarnings("unchecked") // ok
            ParameterParser pp = parserService.getParser((Class<ParameterParser>)parameterParserClazz);
            data.get(Turbine.class).put(ParameterParser.class, pp);

            @SuppressWarnings("unchecked") // ok
            CookieParser cp = parserService.getParser((Class<CookieParser>)cookieParserClazz);
            data.get(Turbine.class).put(CookieParser.class, cp);

            Locale locale = req.getLocale();

            if (locale == null)
            {
                // get the default from the Turbine configuration
                locale = data.getLocale();
            }

            // set the locale detected and propagate it to the parsers
            data.setLocale(locale);
        }
        catch (PoolException pe)
        {
            throw new TurbineException("RunData configuration '" + key + "' is illegal caused a pool exception", pe);
        }
        catch (TurbineRuntimeException | ClassCastException | InstantiationException x)
        {
            throw new TurbineException("RunData configuration '" + key + "' is illegal", x);
        }

        // Set the request and response.
        data.get(Turbine.class).put(HttpServletRequest.class, req);
        data.get(Turbine.class).put(HttpServletResponse.class, res);

        // Set the servlet configuration.
        data.get(Turbine.class).put(ServletConfig.class, config);
        data.get(Turbine.class).put(ServletContext.class, config.getServletContext());

        // Set the ServerData.
        data.get(Turbine.class).put(ServerData.class, new ServerData(req));

        return data;
    }

    /**
     * Puts the used RunData object back to the factory for recycling.
     *
     * @param data the used RunData object.
     * @return true, if pooling is supported and the object was accepted.
     */
    @Override
    public boolean putRunData(RunData data)
    {
        if (data instanceof TurbineRunData)
        {
            parserService.putParser(((TurbineRunData) data).getParameterParser());
            parserService.putParser(((TurbineRunData) data).getCookieParser());

            return pool.putInstance(data);
        }
        else
        {
            return false;
        }
    }

    @SuppressWarnings("unchecked") // ok
    private <T> Class<T> classForName(String className) throws TurbineRuntimeException
    {
        try
        {
            return (Class<T>) Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new TurbineRuntimeException("Could not load class " + className, e);
        }
    }
}
