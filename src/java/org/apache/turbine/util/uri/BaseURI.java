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


import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServerData;

/**
 * This is the base class for all dynamic URIs in the Turbine System.
 *
 * All of the classes used for generating URIs are derived from this.
 *
 * @author <a href="mailto:jon@clearink.com">Jon S. Stevens</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */

public abstract class BaseURI
        implements URI,
                   URIConstants
{
    /** Logging */
    private static final Logger log = LogManager.getLogger(BaseURI.class);

    /** ServerData Object for scheme, name, port etc. */
    private ServerData serverData =
            new ServerData(null, HTTP_PORT, HTTP, null, null);

    /** Whether we want to redirect or not. */
    private boolean redirect = false;

    /** Servlet response interface. */
    private HttpServletResponse response = null;

    /** Reference Anchor (#ref) */
    private String reference = null;

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
     *
     */
    public BaseURI()
    {
        init(Turbine.getDefaultServerData());
        setResponse(null);
    }

    /**
     * Constructor with a RunData object
     *
     * @param runData A RunData object
     */
    public BaseURI(RunData runData)
    {
        init(runData.getServerData());
        setResponse(runData.getResponse());
    }

    /**
     * Constructor, set explicit redirection
     *
     * @param runData A RunData object
     * @param redirect True if redirection allowed.
     */
    public BaseURI(RunData runData, boolean redirect)
    {
        init(runData.getServerData());
        setResponse(runData.getResponse());
        setRedirect(redirect);
    }

    /**
     * Constructor with a ServerData object
     *
     * @param serverData A ServerData object
     */
    public BaseURI(ServerData serverData)
    {
        init(serverData);
        setResponse(null);
    }

    /**
     * Constructor, set explicit redirection
     *
     * @param serverData A ServerData object
     * @param redirect True if redirection allowed.
     */
    public BaseURI(ServerData serverData, boolean redirect)
    {
        init(serverData);
        setResponse(null);
        setRedirect(redirect);
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
     * Init with a ServerData object
     *
     * @param serverData A ServerData object
     *
     */
    private void init(ServerData serverData)
    {
        log.debug("init({})", serverData);

        if (serverData != null)
        {
            // We must clone this, because if BaseURI is used in a pull tool,
            // then the fields might be changed. If we don't clone, this might pull
            // through to the ServerData object saved at firstRequest() in the
            // Turbine object.
            this.serverData = (ServerData) serverData.clone();
        }
        else
        {
            log.error("Passed null ServerData object!");
        }
        reference = null;
    }

    /*
     * ========================================================================
     *
     * Getter / Setter
     *
     * ========================================================================
     *
     */

    /**
     * Set the redirect Flag
     *
     * @param redirect The new value of the redirect flag.
     */
    public void setRedirect(boolean redirect)
    {
        this.redirect = redirect;
    }

    /**
     * Returns the current value of the Redirect flag
     *
     * @return True if Redirect is allowed
     *
     */
    public boolean isRedirect()
    {
        return redirect;
    }

    /**
     * Gets the script name (/servlets/Turbine).
     *
     * @return A String with the script name.
     */
    @Override
    public String getScriptName()
    {
        return serverData.getScriptName();
    }

    /**
     * Sets the script name (/servlets/Turbine).
     *
     * @param scriptName A String with the script name.
     */
    public void setScriptName(String scriptName)
    {
        serverData.setScriptName(scriptName);
    }

    /**
     * Gets the context path.
     *
     * @return A String with the context path.
     */
    @Override
    public String getContextPath()
    {
        return serverData.getContextPath();
    }

    /**
     * Sets the context path.
     *
     * @param contextPath A String with the context path
     */
    public void setContextPath(String contextPath)
    {
        serverData.setContextPath(contextPath);
    }

    /**
     * Gets the server name.
     *
     * @return A String with the server name.
     */
    @Override
    public String getServerName()
    {
        return serverData.getServerName();
    }

    /**
     * Sets the server name.
     *
     * @param serverName A String with the server name.
     */
    public void setServerName(String serverName)
    {
        serverData.setServerName(serverName);
    }

    /**
     * Gets the server port.
     *
     * @return A String with the server port.
     */
    @Override
    public int getServerPort()
    {
        int serverPort = serverData.getServerPort();

        if (serverPort == 0)
        {
            if(getServerScheme().equals(HTTPS))
            {
                serverPort = HTTPS_PORT;
            }
            else
            {
                serverPort = HTTP_PORT;
            }
        }
        return serverPort;
    }

    /**
     * Sets the server port.
     *
     * @param serverPort An int with the port.
     */
    public void setServerPort(int serverPort)
    {
        serverData.setServerPort(serverPort);
    }

    /**
     * Method to specify that a URI should use SSL. The default port
     * is used.
     */
    public void setSecure()
    {
        setSecure(HTTPS_PORT);
    }

    /**
     * Method to specify that a URI should use SSL.
     * Whether or not it does is determined from Turbine.properties.
     * If use.ssl in the Turbine.properties is set to false, then
     * http is used in any case. (Default of use.ssl is true).
     *
     * @param port An int with the port number.
     */
    public void setSecure(int port)
    {
        boolean useSSL =
                Turbine.getConfiguration()
                .getBoolean(TurbineConstants.USE_SSL_KEY,
                        TurbineConstants.USE_SSL_DEFAULT);

        setServerScheme(useSSL ? HTTPS : HTTP);
        setServerPort(port);
    }

    /**
     * Sets the scheme (HTTP or HTTPS).
     *
     * @param serverScheme A String with the scheme.
     */
    public void setServerScheme(String serverScheme)
    {
        serverData.setServerScheme(StringUtils.isNotEmpty(serverScheme)
                ? serverScheme : "");
    }

    /**
     * Returns the current Server Scheme
     *
     * @return The current Server scheme
     *
     */
    @Override
    public String getServerScheme()
    {
        String serverScheme = serverData.getServerScheme();

        return StringUtils.isNotEmpty(serverScheme) ? serverScheme : HTTP;
    }

    /**
     * Sets a reference anchor (#ref).
     *
     * @param reference A String containing the reference.
     */
    public void setReference(String reference)
    {
        this.reference = reference;
    }

    /**
     * Returns the current reference anchor.
     *
     * @return A String containing the reference.
     */
    @Override
    public String getReference()
    {
        return hasReference() ? reference : "";
    }

    /**
     * Does this URI contain an anchor? (#ref)
     *
     * @return True if this URI contains an anchor.
     */
    public boolean hasReference()
    {
        return StringUtils.isNotEmpty(reference);
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
     * Set a Response Object to use when creating the
     * response string.
     *
     * @param response the servlet response
     */
    protected void setResponse(HttpServletResponse response)
    {
        this.response = response;
    }

    /**
     * Returns the Response Object from the Servlet Container.
     *
     * @return The Servlet Response object or null
     *
     */
    protected HttpServletResponse getResponse()
    {
        return response;
    }

    /**
     * Append the Context Path and Script Name to the passed
     * String Buffer.
     *
     * <p>
     * This is a convenience method to be
     * used in the Link output routines of derived classes to
     * easily append the correct path.
     * </p>
     *
     * @param sb The StringBuilder to store context path and script name.
     */
    protected void getContextAndScript(StringBuilder sb)
    {
        String context = getContextPath();

        if(StringUtils.isNotEmpty(context))
        {
            if(context.charAt(0) != '/')
            {
                sb.append('/');
            }
            sb.append (context);
        }

        // /servlet/turbine
        String script = getScriptName();

        if(StringUtils.isNotEmpty(script))
        {
            if(script.charAt(0) != '/')
            {
                sb.append('/');
            }
            sb.append (script);
        }
    }

    /**
     * Appends Scheme, Server and optionally the port to the
     * supplied String Buffer.
     *
     * <p>
     * This is a convenience method to be
     * used in the Link output routines of derived classes to
     * easily append the correct server scheme.
     * </p>
     *
     * @param sb The StringBuilder to store the scheme and port information.
     */
    protected void getSchemeAndPort(StringBuilder sb)
    {
        // http(s)://<servername>
        sb.append(getServerScheme());
        sb.append(URIConstants.URI_SCHEME_SEPARATOR);
        sb.append(getServerName());

        // (:<port>)
        if ((getServerScheme().equals(HTTP)
                    && getServerPort() != HTTP_PORT)
                || (getServerScheme().equals(HTTPS)
                        && getServerPort() != HTTPS_PORT))
        {
            sb.append(':');
            sb.append(getServerPort());
        }
    }

    /**
     * Encodes a Response Uri according to the Servlet Container.
     * This might add a Java session identifier or do redirection.
     * The resulting String can be used in a page or template.
     *
     * @param uri The Uri to encode
     *
     * @return An Uri encoded by the container.
     */
    protected String encodeResponse(String uri)
    {
        String res = uri;

        HttpServletResponse response = getResponse();

        if(response == null)
        {
            log.debug("No Response Object!");
        }
        else
        {
            try
            {
                if(isRedirect())
                {
                    log.debug("Should Redirect");
                    res = response.encodeRedirectURL(uri);
                }
                else
                {
                    res = response.encodeURL(uri);
                }
            }
            catch(Exception e)
            {
                log.error("response" + response + ", uri: " + uri);
                log.error("While trying to encode the URI: ", e);
            }
        }

        log.debug("encodeResponse():  " + res);
        return res;
    }
}
