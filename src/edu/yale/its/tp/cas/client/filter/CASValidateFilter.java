/*  Copyright (c) 2000-2004 Yale University. All rights reserved. 
 *  See full notice at end.
 */

package edu.yale.its.tp.cas.client.filter;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import edu.yale.its.tp.cas.client.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Validates tickets on requests for the filtered path.
 * </p>
 * <p>
 * Very much like CASFilter, except only performs validation, not redirection,
 * and performs no authorization role.  Does not check for the existence or identity of the
 * immediately previous service in the proxy chain, if any.  Does not, unlike the CASFilter itself,
 * require anything of a CASReceipt already stored in the session.  This filter exists merely
 * to validate and expose the results of valid, authentic CAS tickets.
 * </p>
 * <p>Subsequent filters can be used to perform authorization requirements
 * on the CASReceipt or translate the receipt into information that the client application
 * can understand and consume.</p>
 * 
 * <p>
 * The following filter initialization parameters should be declared in
 * <code>web.xml</code>:
 * </p>
 * 
 * <ul>
 * <li><code>edu.yale.its.tp.cas.client.filter.validateUrl</code>: URL to
 * validation URL on CAS server. (Required)</li>
 * <li><code>edu.yale.its.tp.cas.client.filter.serviceUrl</code>: URL of
 * this service. (Required if <code>serverName</code> is not specified)</li>
 * <li><code>edu.yale.its.tp.cas.client.filter.serverName</code>: full
 * hostname with port number (e.g. <code>www.foo.com:8080</code>). Port
 * number isn't required if it is standard (80 for HTTP, 443 for HTTPS).
 * (Required if <code>serviceUrl</code> is not specified)</li>
 * <li><code>edu.yale.its.tp.cas.client.filter.proxyCallbackUrl</code>: URL
 * of local proxy callback listener used to acquire PGT/PGTIOU. (Optional.)
 * </li>
 * <li><code>edu.yale.its.tp.cas.client.filter.renew</code>: value of CAS
 * "renew" parameter. Bypasses single sign-on and requires user to provide CAS
 * with his/her credentials again. (Optional. If nothing is specified, this
 * defaults to false.)</li>
 * <li><code>edu.yale.its.tp.cas.client.filter.wrapRequest</code>: wrap the
 * <code>HttpServletRequest</code> object, overriding the
 * <code>getRemoteUser()</code> method. When set to "true",
 * <code>request.getRemoteUser()</code> will return the username of the
 * currently logged-in CAS user. (Optional. If nothing is specified, this
 * defaults to false.)</li>
 * </ul>
 * 
 * <p>
 * The logged-in username is set in the session attribute defined by the value
 * of <code>CAS_FILTER_USER</code> and may be accessed from within your
 * application either by setting <code>wrapRequest</code> and calling
 * <code>request.getRemoteUser()</code>, or by calling
 * <code>session.getAttribute(CASFilter.CAS_FILTER_USER)</code>.
 * </p>
 * 
 * <p>
 * The filter also exposes a CASReceipt representing the fruits of the
 * authentication which may be accessed by client code by calling
 * <code>session.getAttribute(CASFilter.CAS_FILTER_RECEIPT)</code>.
 * </p>
 * 
 * <p>
 * If <code>proxyCallbackUrl</code> is set, the URL will be passed to CAS upon
 * validation. If the callback URL is valid, it will receive a CAS PGT and a
 * PGTIOU. The PGTIOU will be returned to this filter and will be accessible
 * through the CASReceipt. You may then acquire proxy tickets to other services
 * by calling
 * <code>edu.yale.its.tp.cas.proxy.ProxyTicketReceptor.getProxyTicket(pgtIou, targetService)</code>.
 * 
 * Based on the CASFilter by Shawn Bayern and Drew Mazurek.
 * 
 * @author andrew.petro@yale.edu
 */
public class CASValidateFilter implements Filter {

    private static Log log = LogFactory.getLog(CASValidateFilter.class);

    // Filter initialization parameters

    /**
     * The name of the filter initialization parameter the value of which must
     * be the https: address of the CAS Validate servlet. Must be a CAS 2.0
     * validate servlet (CAS 1.0 non-XML won't suffice). Required parameter.
     */
    public final static String VALIDATE_INIT_PARAM = "edu.yale.its.tp.cas.client.filter.validateUrl";

    /**
     * The name of the filter initialization parameter the value of which must
     * be the address of the service this filter is filtering. The filter will
     * use this as the service parameter for CAS login and validation. Either
     * this parameter or SERVERNAME_INIT_PARAM must be set.
     */
    public final static String SERVICE_INIT_PARAM = "edu.yale.its.tp.cas.client.filter.serviceUrl";

    /**
     * The name of the filter initialization parameter the vlaue of which must
     * be the server name, e.g. www.yale.edu , of the service this filter is
     * filtering. The filter will construct from this name and the request the
     * full service parameter for CAS login and validation.
     */
    public final static String SERVERNAME_INIT_PARAM = "edu.yale.its.tp.cas.client.filter.serverName";

    /**
     * The name of the filter initialization parameter the value of which must
     * be the String that should be sent as the "renew" parameter on the request
     * for login and validation. This should either be "true" or not be set. It
     * is mutually exclusive with GATEWAY.
     */
    public final static String RENEW_INIT_PARAM = "edu.yale.its.tp.cas.client.filter.renew";

    /**
     * The name of the filter initialization parameter the value of which must
     * be the https: URL to which CAS should send Proxy Granting Tickets when
     * this filter validates tickets.
     */
    public final static String PROXY_CALLBACK_INIT_PARAM = "edu.yale.its.tp.cas.client.filter.proxyCallbackUrl";

    /**
     * The name of the filter initialization parameter the value of which
     * indicates whether this filter should wrap requests to expose the
     * authenticated username.
     */
    public final static String WRAP_REQUESTS_INIT_PARAM = "edu.yale.its.tp.cas.client.filter.wrapRequest";

    // Session attributes used by this filter

    /**
     * <p>
     * Session attribute in which the username is stored.
     * </p>
     */
    public final static String CAS_FILTER_USER = "edu.yale.its.tp.cas.client.filter.user";

    /**
     * Session attribute in which the CASReceipt is stored.
     */
    public final static String CAS_FILTER_RECEIPT = "edu.yale.its.tp.cas.client.filter.receipt";

    //*********************************************************************
    // Configuration state

    /** Secure URL whereat CAS offers its CAS 2.0 validate service */
    private String casValidate;

    /** Filtered service URL for use as service parameter to login and validate */
    private String casServiceUrl;

    /**
     * Name of server, for use in assembling service URL for use as service
     * parameter to login and validate.
     */
    private String casServerName;

    /**
     * Secure URL whereto this filter should ask CAS to send Proxy Granting
     * Tickets.
     */
    private String casProxyCallbackUrl;

    /** True if renew parameter should be set on login and validate */
    private boolean casRenew;

    /**
     * True if this filter should wrap requests to expose authenticated user as
     * getRemoteUser();
     */
    private boolean wrapRequest;

    //*********************************************************************
    // Initialization

    public void init(FilterConfig config) throws ServletException {
        casValidate = config.getInitParameter(VALIDATE_INIT_PARAM);
        casServiceUrl = config.getInitParameter(SERVICE_INIT_PARAM);
        casRenew = Boolean.valueOf(config.getInitParameter(RENEW_INIT_PARAM))
                .booleanValue();
        casServerName = config.getInitParameter(SERVERNAME_INIT_PARAM);
        casProxyCallbackUrl = config
                .getInitParameter(PROXY_CALLBACK_INIT_PARAM);
        wrapRequest = Boolean.valueOf(
                config.getInitParameter(WRAP_REQUESTS_INIT_PARAM))
                .booleanValue();

        if (casServerName != null && casServiceUrl != null) {
            throw new ServletException(
                    "serverName and serviceUrl cannot both be set: choose one.");
        }
        if (casServerName == null && casServiceUrl == null) {
            throw new ServletException(
                    "one of serverName or serviceUrl must be set.");
        }
        if (casServiceUrl != null) {
            if (!(casServiceUrl.startsWith("https://") || (casServiceUrl
                    .startsWith("http://")))) {
                throw new ServletException(
                        "service URL must start with http:// or https://; its current value is ["
                                + casServiceUrl + "]");
            }
        }

        if (casValidate == null) {
            throw new ServletException("validateUrl parameter must be set.");
        }
        if (!casValidate.startsWith("https://")) {
            throw new ServletException(
                    "validateUrl must start with https://, its current value is ["
                            + casValidate + "]");
        }

        if (log.isDebugEnabled()) {
            log
                    .debug(("CASValidateFilter initialized as: [" + toString() + "]"));
        }
    }

    //*********************************************************************
    // Filter processing

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain fc) throws ServletException, IOException {

        if (log.isTraceEnabled()) {
            log.trace("entering doFilter()");
        }

        // make sure we've got an HTTP request
        if (!(request instanceof HttpServletRequest)
                || !(response instanceof HttpServletResponse)) {
            log
                    .error("doFilter() called on a request or response that was not an HttpServletRequest or response.");
            throw new ServletException("CASFilter protects only HTTP resources");
        }

        // Is this a request for the proxy callback listener? If so, pass
        // it through
        if (casProxyCallbackUrl != null
                && casProxyCallbackUrl.endsWith(((HttpServletRequest) request)
                        .getRequestURI())
                && request.getParameter("pgtId") != null
                && request.getParameter("pgtIou") != null) {
            log
                    .trace("passing through what we hope is CAS's request for proxy ticket receptor.");
            fc.doFilter(request, response);
            return;
        }

        // Wrap the request if desired
        if (wrapRequest) {
            log.trace("Wrapping request with CASFilterRequestWrapper.");
            request = new CASFilterRequestWrapper((HttpServletRequest) request);
        }

        HttpSession session = ((HttpServletRequest) request).getSession();

        // if our attribute's already present and valid, pass through the filter
        // chain
        CASReceipt receipt = (CASReceipt) session
                .getAttribute(CAS_FILTER_RECEIPT);
        if (receipt != null) {
            log
                    .trace("CAS_FILTER_RECEIPT attribute was present - passing  request through filter..");
            fc.doFilter(request, response);
            return;
        }

        // otherwise, we need to authenticate via CAS
        String ticket = request.getParameter("ticket");

        // no ticket? no validation to be done, pass the request through.
        if (ticket == null || ticket.equals("")) {
            log.trace("CAS ticket was not present on request.");
            fc.doFilter(request, response);
            return;
        }

        try {
            receipt = getAuthenticatedUser((HttpServletRequest) request);
        } catch (CASAuthenticationException e) {
            log.error(e);
            throw new ServletException(e);
        }

        // Store the authenticated user in the session
        if (session != null) { // probably unnecessary
            session.setAttribute(CAS_FILTER_USER, receipt.getUserName());
            session.setAttribute(CASValidateFilter.CAS_FILTER_RECEIPT, receipt);
        }
        if (log.isTraceEnabled()) {
            log.trace("validated ticket to get authenticated receipt ["
                    + receipt + "], now passing request along filter chain.");
        }

        // continue processing the request
        fc.doFilter(request, response);
        log.trace("returning from doFilter()");
    }

    //*********************************************************************
    // Utility methods

    /**
     * Converts a ticket parameter to a CASReceipt.
     * @param request - request bearing the ticket parameter
     * @return CASReceipt representing results of ticket validation
     * @throws ServletException -
     *             when unable to get service for request
     * @throws CASAuthenticationException -
     *             on authentication failure
     */
    private CASReceipt getAuthenticatedUser(HttpServletRequest request)
            throws ServletException, CASAuthenticationException {
        log.trace("entering getAuthenticatedUser()");
        ProxyTicketValidator pv = null;

        pv = new ProxyTicketValidator();
        pv.setCasValidateUrl(casValidate);
        pv.setServiceTicket(request.getParameter("ticket"));
        pv.setService(getService(request));
        pv.setRenew(Boolean.valueOf(casRenew).booleanValue());
        if (casProxyCallbackUrl != null) {
            pv.setProxyCallbackUrl(casProxyCallbackUrl);
        }
        if (log.isDebugEnabled()) {
            log.debug("about to validate ProxyTicketValidator: [" + pv + "]");
        }

        return CASReceipt.getReceipt(pv);

    }

    /**
     * Returns either the configured service or figures it out for the current
     * request. The returned service is URL-encoded.
     */
    private String getService(HttpServletRequest request)
            throws ServletException {

        log.trace("entering getService()");
        String serviceString;

        // ensure we have a server name or service name
        if (casServerName == null && casServiceUrl == null)
            throw new ServletException(
                    "need one of the following configuration "
                            + "parameters: edu.yale.its.tp.cas.client.filter.serviceUrl or "
                            + "edu.yale.its.tp.cas.client.filter.serverName");

        // use the given string if it's provided
        if (casServiceUrl != null)
            serviceString = URLEncoder.encode(casServiceUrl);
        else
            // otherwise, return our best guess at the service
            serviceString = Util.getService(request, casServerName);
        if (log.isTraceEnabled()) {
            log.trace("returning from getService() with service ["
                    + serviceString + "]");
        }
        return serviceString;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[CASValidateFilter:");

        sb.append(" wrapRequest=");
        sb.append(this.wrapRequest);

        sb.append(" casProxyCallbackUrl=[");
        sb.append(casProxyCallbackUrl);
        sb.append("]");

        if (this.casRenew) {
            sb.append(" casRenew=true");
        }

        sb.append(" casServerName=[");
        sb.append(casServerName);
        sb.append("]");

        sb.append(" casServiceUrl=[");
        sb.append(casServiceUrl);
        sb.append("]");

        sb.append(" casValidate=[");
        sb.append(casValidate);
        sb.append("]");

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
    }
}

/*
 * Copyright (c) 2004 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met:
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */
