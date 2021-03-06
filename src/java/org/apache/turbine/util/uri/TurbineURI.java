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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.parser.ParserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServerData;

/**
 * This class allows you to keep all the information needed for a single
 * link at one place. It keeps your query data, path info, the server
 * scheme, name, port and the script path.
 *
 * If you must generate a Turbine Link, use this class.
 *
 * @author <a href="mailto:jon@clearink.com">Jon S. Stevens</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */

public class TurbineURI
        extends BaseURI
{
    /** Logging */
    private static final Logger log = LogManager.getLogger(TurbineURI.class);

    /** Contains the PathInfo and QueryData vectors */
    private List<URIParam> [] dataVectors = null;

    /** Local reference to the parser service for URI parameter folding */
    private ParserService parserService;

    /** URI Parameter encoding as defined by the parser service */
    private Charset parameterEncoding;

    /*
     * ========================================================================
     *
     * Constructors
     *
     * ========================================================================
     *
     */

    /**
     * Empty C'tor. Uses Turbine.getDefaultServerData().
     */
    public TurbineURI()
    {
        super();
        init();
    }

    /**
     * Constructor with a RunData object.
     *
     * @param runData A RunData object
     */
    public TurbineURI(RunData runData)
    {
        super(runData);
        init();
    }

    /**
     * Constructor, set explicit redirection.
     *
     * @param runData A RunData object
     * @param redirect True if redirection allowed.
     */
    public TurbineURI(RunData runData, boolean redirect)
    {
        super(runData, redirect);
        init();
    }

    /**
     * Constructor, set Screen.
     *
     * @param runData A RunData object
     * @param screen A Screen Name
     */
    public TurbineURI(RunData runData, String screen)
    {
        this(runData);
        setScreen(screen);
    }

    /**
     * Constructor, set Screen, set explicit redirection.
     *
     * @param runData A RunData object
     * @param screen A Screen Name
     * @param redirect True if redirection allowed.
     */
    public TurbineURI(RunData runData, String screen, boolean redirect)
    {
        this(runData, redirect);
        setScreen(screen);
    }

    /**
     * Constructor, set Screen and Action.
     *
     * @param runData A RunData object
     * @param screen A Screen Name
     * @param action An Action Name
     */
    public TurbineURI(RunData runData, String screen, String action)
    {
        this(runData, screen);
        setAction(action);
    }

    /**
     * Constructor, set Screen and Action, set explicit redirection.
     *
     * @param runData A RunData object
     * @param screen A Screen Name
     * @param action An Action Name
     * @param redirect True if redirection allowed.
     */
    public TurbineURI(RunData runData, String screen, String action, boolean redirect)
    {
        this(runData, screen, redirect);
        setAction(action);
    }

    /**
     * Constructor with a ServerData object.
     *
     * @param serverData A ServerData object
     */
    public TurbineURI(ServerData serverData)
    {
        super(serverData);
        init();
    }

    /**
     * Constructor, set explicit redirection.
     *
     * @param serverData A ServerData object
     * @param redirect True if redirection allowed.
     */
    public TurbineURI(ServerData serverData, boolean redirect)
    {
        super(serverData, redirect);
        init();
    }

    /**
     * Constructor, set Screen.
     *
     * @param serverData A ServerData object
     * @param screen A Screen Name
     */
    public TurbineURI(ServerData serverData, String screen)
    {
        this(serverData);
        setScreen(screen);
    }

    /**
     * Constructor, set Screen, set explicit redirection.
     *
     * @param serverData A ServerData object
     * @param screen A Screen Name
     * @param redirect True if redirection allowed.
     */
    public TurbineURI(ServerData serverData, String screen, boolean redirect)
    {
        this(serverData, redirect);
        setScreen(screen);
    }

    /**
     * Constructor, set Screen and Action.
     *
     * @param serverData A ServerData object
     * @param screen A Screen Name
     * @param action An Action Name
     */
    public TurbineURI(ServerData serverData, String screen, String action)
    {
        this(serverData, screen);
        setAction(action);
    }

    /**
     * Constructor, set Screen and Action, set explicit redirection.
     *
     * @param serverData A ServerData object
     * @param screen A Screen Name
     * @param action An Action Name
     * @param redirect True if redirection allowed.
     */
    public TurbineURI(ServerData serverData, String screen, String action,
                      boolean redirect)
    {
        this(serverData, screen, redirect);
        setAction(action);
    }

    /**
     * Constructor, user Turbine.getDefaultServerData(), set Screen and Action.
     *
     * @param screen A Screen Name
     * @param action An Action Name
     */
    public TurbineURI(String screen, String action)
    {
        this();
        setScreen(screen);
        setAction(action);
    }

    /*
     * ========================================================================
     *
     * Init
     *
     * ========================================================================
     *
     */

    /**
     * Init the TurbineURI.
     */
    @SuppressWarnings("unchecked")
    private void init()
    {
        dataVectors = new List[2];
        dataVectors[PATH_INFO]  = new ArrayList<>();
        dataVectors[QUERY_DATA] = new ArrayList<>();
        parserService = (ParserService)TurbineServices.getInstance().getService(ParserService.ROLE);

        try
        {
            parameterEncoding = Charset.forName(parserService.getParameterEncoding());
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException e)
        {
            log.error("Unsupported encoding {}", parserService.getParameterEncoding(), e);
        }
    }

    /**
     * Sets the action= value for this URL.
     *
     * By default it adds the information to the path_info instead
     * of the query data. An empty value (null or "") cleans out
     * an existing value.
     *
     * @param action A String with the action value.
     */
    public void setAction(String action)
    {
        if(StringUtils.isNotEmpty(action))
        {
            add(PATH_INFO, CGI_ACTION_PARAM, action);
        }
        else
        {
            clearAction();
        }
    }

    /**
     * Sets the fired eventSubmit= value for this URL.
     *
     * @param event The event to fire.
     *
     */
    public void setEvent(String event)
    {
        add(PATH_INFO, EVENT_PREFIX + event, event);
    }

    /**
     * Sets the action= and eventSubmit= values for this URL.
     *
     * By default it adds the information to the path_info instead
     * of the query data. An empty value (null or "") for the action cleans out
     * an existing value.  An empty value (null or "") for the event has no
     * effect.
     *
     * @param action A String with the action value.
     * @param event A string with the event name.
     */
    public void setActionEvent(String action, String event)
    {
        setAction(action);
        if(StringUtils.isNotEmpty(event))
        {
            setEvent(event);
        }
    }

    /**
     * Clears the action= value for this URL.
     */
    public void clearAction()
    {
        removePathInfo(CGI_ACTION_PARAM);
    }

    /**
     * Sets the screen= value for this URL.
     *
     * By default it adds the information to the path_info instead
     * of the query data. An empty value (null or "") cleans out
     * an existing value.
     *
     * @param screen A String with the screen value.
     */
    public void setScreen(String screen)
    {
        if(StringUtils.isNotEmpty(screen))
        {
            add(PATH_INFO, CGI_SCREEN_PARAM, screen);
        }
        else
        {
            clearScreen();
        }
    }

    /**
     * Clears the screen= value for this URL.
     */
    public void clearScreen()
    {
        removePathInfo(CGI_SCREEN_PARAM);
    }

    /*
     * ========================================================================
     *
     * Adding and removing Data from the Path Info and Query Data
     *
     * ========================================================================
     */


    /**
     * Adds a name=value pair for every entry in a ParameterParser
     * object to the path_info string.
     *
     * @param pp A ParameterParser.
     */
    public void addPathInfo(ParameterParser pp)
    {
        add(PATH_INFO, pp);
    }

    /**
     * Adds an existing List of URIParam objects to
     * the path_info string.
     *
     * @param list A list with URIParam objects.
     */
    public void addPathInfo(List<URIParam> list)
    {
        add(PATH_INFO, list);
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value An Object with the value to add.
     */
    public void addPathInfo(String name, Object value)
    {
        add(PATH_INFO, name, null == value ? null : value.toString());
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A String with the value to add.
     */
    public void addPathInfo(String name, String value)
    {
        add(PATH_INFO, name, value);
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A double with the value to add.
     */
    public void addPathInfo(String name, double value)
    {
        add(PATH_INFO, name, Double.toString(value));
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value An int with the value to add.
     */
    public void addPathInfo(String name, int value)
    {
        add(PATH_INFO, name, Integer.toString(value));
    }

    /**
     * Adds a name=value pair to the path_info string.
     *
     * @param name A String with the name to add.
     * @param value A long with the value to add.
     */
    public void addPathInfo(String name, long value)
    {
        add(PATH_INFO, name, Long.toString(value));
    }

    /**
     * Adds a name=value pair to the query string.
     *
     * @param name A String with the name to add.
     * @param value An Object with the value to add.
     */
    public void addQueryData(String name, Object value)
    {
        add(QUERY_DATA, name, null == value ? null : value.toString());
    }

    /**
     * Adds a name=value pair to the query string.
     *
     * @param name A String with the name to add.
     * @param value A String with the value to add.
     */
    public void addQueryData(String name, String value)
    {
        add(QUERY_DATA, name, value);
    }

    /**
     * Adds a name=value pair to the query string.
     *
     * @param name A String with the name to add.
     * @param value A double with the value to add.
     */
    public void addQueryData(String name, double value)
    {
        add(QUERY_DATA, name, Double.toString(value));
    }

    /**
     * Adds a name=value pair to the query string.
     *
     * @param name A String with the name to add.
     * @param value An int with the value to add.
     */
    public void addQueryData(String name, int value)
    {
        add(QUERY_DATA, name, Integer.toString(value));
    }

    /**
     * Adds a name=value pair to the query string.
     *
     * @param name A String with the name to add.
     * @param value A long with the value to add.
     */
    public void addQueryData(String name, long value)
    {
        add(QUERY_DATA, name, Long.toString(value));
    }

    /**
     * Adds a name=value pair for every entry in a ParameterParser
     * object to the query string.
     *
     * @param pp A ParameterParser.
     */
    public void addQueryData(ParameterParser pp)
    {
        add(QUERY_DATA, pp);
    }

    /**
     * Adds an existing List of URIParam objects to the query data.
     *
     * @param list A list with URIParam objects.
     */
    public void addQueryData(List<URIParam> list)
    {
        add(QUERY_DATA, list);
    }

    /**
     * Is Path Info data set in this URI?
     *
     * @return true if Path Info has values
     */
    public boolean hasPathInfo()
    {
        return !dataVectors[PATH_INFO].isEmpty();
    }

    /**
     * Removes all the path info elements.
     */
    public void removePathInfo()
    {
        dataVectors[PATH_INFO].clear();
    }

    /**
     * Removes a name=value pair from the path info.
     *
     * @param name A String with the name to be removed.
     */
    public void removePathInfo(String name)
    {
        remove(PATH_INFO, name);
    }

    /**
     * Is Query data set in this URI?
     *
     * @return true if Query data has values
     */
    public boolean hasQueryData()
    {
        return !dataVectors[QUERY_DATA].isEmpty();
    }

    /**
     * Removes all the query string elements.
     */
    public void removeQueryData()
    {
        dataVectors[QUERY_DATA].clear();
    }

    /**
     * Removes a name=value pair from the query string.
     *
     * @param name A String with the name to be removed.
     */
    public void removeQueryData(String name)
    {
        remove (QUERY_DATA, name);
    }

    /**
     * Template Link and friends want to be able to turn the encoding
     * of the servlet container off. After calling this method,
     * the no encoding will happen any longer. If you think, that you
     * need this outside a template context, think again.
     */
    public void clearResponse()
    {
        setResponse(null);
    }


    /**
     * Builds the URL with all of the data URL-encoded as well as
     * encoded using HttpServletResponse.encodeUrl(). The resulting
     * URL is absolute; it starts with http/https...
     *
     * <pre>
     * TurbineURI tui = new TurbineURI (data, "UserScreen");
     * tui.addPathInfo("user","jon");
     * tui.getAbsoluteLink();
     * </pre>
     *
     *  The above call to absoluteLink() would return the String:
     *
     * <p>
     * http://www.server.com/servlets/Turbine/screen/UserScreen/user/jon
     * </p>
     *
     * @return A String with the built URL.
     */
    public String getAbsoluteLink()
    {
        StringBuilder output = new StringBuilder();

        getSchemeAndPort(output);

        buildRelativeLink(output);

        //
        // Encode Response does all the fixup for the Servlet Container
        //
        return encodeResponse(output.toString());
    }

    /**
     * Builds the URL with all of the data URL-encoded as well as
     * encoded using HttpServletResponse.encodeUrl(). The resulting
     * URL is relative to the webserver root.
     *
     * <pre>
     * TurbineURI tui = new TurbineURI (data, "UserScreen");
     * tui.addPathInfo("user","jon");
     * tui.getRelativeLink();
     * </pre>
     *
     *  The above call to relativeLink() would return the String:
     *
     * <p>
     * /servlets/Turbine/screen/UserScreen/user/jon
     * </p>
     *
     * @return A String with the built URL.
     */
    public String getRelativeLink()
    {
        StringBuilder output = new StringBuilder();

        buildRelativeLink(output);

        //
        // Encode Response does all the fixup for the Servlet Container
        //
        return encodeResponse(output.toString());
    }

    /**
     * Add everything needed for a relative link to the passed StringBuilder.
     *
     * @param output A Stringbuffer
     */
    private void buildRelativeLink(StringBuilder output)
    {
        getContextAndScript(output);

        if (hasPathInfo())
        {
            output.append('/');
            getPathInfoAsString(output);
        }

        if (hasReference())
        {
            output.append('#');
            output.append(getReference());
        }

        if (hasQueryData())
        {
            output.append('?');
            getQueryDataAsString(output);
        }
    }

    /**
     * Gets the current Path Info List.
     *
     * @return A List which contains all query data keys. The keys
     * are URIParam objects.
     */
    public List<URIParam> getPathInfo()
    {
        return dataVectors[PATH_INFO];
    }

    /**
     * Sets the Query Data List. Replaces the current query data list
     * with the one supplied. The list must contain only URIParam
     * objects!
     *
     * @param pathInfo A List with new param objects.
     */

    public void setPathInfo(List<URIParam> pathInfo)
    {
        dataVectors[PATH_INFO] = pathInfo;
    }

    /**
     * Gets the current Query Data List.
     *
     * @return A List which contains all query data keys. The keys
     * are URIParam objects.
     */
    public List<URIParam> getQueryData()
    {
        return dataVectors[QUERY_DATA];
    }

    /**
     * Sets the Query Data List. Replaces the current query data list
     * with the one supplied. The list must contain only URIParam
     * objects!
     *
     * @param queryData A List with new param objects.
     */

    public void setQueryData(List<URIParam> queryData)
    {
        dataVectors[QUERY_DATA] = queryData;
    }

    /**
     * Simply calls getAbsoluteLink(). You should not use this in your
     * code unless you have to. Use getAbsoluteLink.
     *
     * @return This URI as a String
     *
     */
    @Override
    public String toString()
    {
        return getAbsoluteLink();
    }

    /*
     * ========================================================================
     *
     * Protected / Private Methods
     *
     * ========================================================================
     *
     */

    /**
     * Returns the Path Info data as a String.
     *
     * @param output The StringBuilder that should hold the path info.
     */
    private void getPathInfoAsString(StringBuilder output)
    {
        doEncode(output, dataVectors[PATH_INFO], "/", "/");
    }

    /**
     * Returns the Query data as a String.
     *
     * @param output The StringBuilder that should hold the query data.
     */
    private void getQueryDataAsString(StringBuilder output)
    {
        doEncode(output, dataVectors[QUERY_DATA], "&", "=");
    }

    /**
     * URL encode the given string, catching possible Exceptions
     *
     * @param string the string
     * @return the encoded string
     */
    private String urlEncode(String string)
    {
        try
        {
            // Java10: return URLEncoder.encode(string, parameterEncoding);
            return URLEncoder.encode(string, parameterEncoding.name());
        }
        catch (UnsupportedEncodingException e)
        {
            log.warn("Unsupported encoding {}", parameterEncoding);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Does the actual encoding for pathInfoAsString and queryDataAsString.
     *
     * @param output The String builder that should contain the information.
     * @param list A list of key to value pairs
     * @param fieldDelim A char which is used to separate key/value pairs
     * @param valueDelim A char which is used to separate key and value
     */
    private void doEncode(StringBuilder output, Collection<URIParam> list, String fieldDelim, String valueDelim)
    {
        if(!list.isEmpty())
        {
            output.append(list.stream()
                    .map(uriParam ->
                        String.join(
                                valueDelim,
                                urlEncode(uriParam.getKey()),
                                urlEncode(Objects.toString(uriParam.getValue()))))
                    .collect(Collectors.joining(fieldDelim)));
        }
    }

    /**
     * If the type is PATH_INFO, then add name/value to the pathInfo
     * hashtable.
     * <p>
     * If the type is QUERY_DATA, then add name/value to the queryData
     * hashtable.
     *
     * @param type Type (PATH_INFO or QUERY_DATA) of insertion.
     * @param name A String with the name to add.
     * @param value A String with the value to add.
     */
    protected void add(int type,
            String name,
            String value)
    {
        URIParam uriParam = new URIParam(parserService.convertAndTrim(name), value);
        dataVectors[type].add(uriParam); // Code so clean you can eat from...
    }

    /**
     * Method for a quick way to add all the parameters in a
     * ParameterParser.
     *
     * <p>If the type is P (0), then add name/value to the pathInfo
     * hashtable.
     *
     * <p>If the type is Q (1), then add name/value to the queryData
     * hashtable.
     *
     * @param type Type of insertion (@see #add(char type, String name, String value))
     * @param pp A ParameterParser.
     */
    protected void add(int type,
            ParameterParser pp)
    {
        for(String key : pp.keySet())
        {
            if (!key.equalsIgnoreCase(CGI_ACTION_PARAM) &&
                    !key.equalsIgnoreCase(CGI_SCREEN_PARAM))
            {
                String[] values = pp.getStrings(key);
                if(values != null)
                {
                    for (String value : values)
                    {
                        add(type, key, value);
                    }
                }
                else
                {
                    add(type, key, "");
                }
            }
        }
    }

    /**
     * Method for a quick way to add all the parameters in a
     * List with URIParam objects.
     *
     * <p>If the type is P (0), then add name/value to the pathInfo
     * hashtable.
     *
     * <p>If the type is Q (1), then add name/value to the queryData
     * hashtable.
     *
     * @param type Type of insertion (@see #add(char type, String name, String value))
     * @param list A List of URIParam objects
     */
    protected void add(int type, List<URIParam> list)
    {
        dataVectors[type].addAll(list);
    }

    /**
     * If the type is P (0), then remove name/value from the
     * pathInfo hashtable.
     *
     * <p>If the type is Q (1), then remove name/value from the
     * queryData hashtable.
     *
     * @param type Type (P or Q) of removal.
     * @param name A String with the name to be removed.
     */
    protected void remove (int type, String name)
    {
        String key = parserService.convertAndTrim(name);

        dataVectors[type].removeIf(uriParam -> key.equals(uriParam.getKey()));
    }
}
