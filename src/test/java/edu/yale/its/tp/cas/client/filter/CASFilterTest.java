/*
 * Created on Jun 22, 2004
 *
 * Copyright(c) Yale University, Jun 22, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.client.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.mockrunner.mock.web.MockFilterConfig;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletContext;

import edu.yale.its.tp.cas.client.CASReceipt;
import junit.framework.TestCase;

/**
 * Test case for CASFilter.
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.2 $ $Date: 2004/07/18 19:48:23 $
 */
public class CASFilterTest extends TestCase {

    /** Test value for authenticated username */
    private static final String USERNAME = "someone";
    /** Test value for CAS validation URL */
    private static final String CAS_VALIDATE_URL = "https://casserver.com/serviceValidate";
    /** Test value for CAS login URL */
    private static final String CAS_LOGIN_URL = "https://casserver.com/login";
    /** Test value for the client application server name */
    private static final String TEST_SERVER_NAME = "www.client.com:8080";
    
    /**
     * A basic servlet context with all the required CAS Filter parameters set.
     */
    MockServletContext basicContext;

    /**
     * A new filterConfig, ready to receive the (potentially modified) basicContext.
     */
    MockFilterConfig mockConfig;

    /**
     * A session with CASFilter.CAS_FILTER_RECEIPT already set.
     */
    MockHttpSession authenticatedSession;

    MockHttpServletRequest mockRequest;
    
    MockHttpServletResponse mockResponse;
    
    CASReceipt basicReceipt;
    
    WatchfulFilterChain filterChain;

    /**
     * Constructor for CASFilterTest.
     * @param name
     */
    public CASFilterTest(String name) {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        // create a basic initialization context for a CASFilter
        this.basicContext = new MockServletContext();
        this.mockConfig = new MockFilterConfig();
        this.mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.loginUrl",
            CAS_LOGIN_URL);
        this.mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.validateUrl",
            CAS_VALIDATE_URL);
        this.mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.serverName",
            TEST_SERVER_NAME);

        
        // create a basic authenticated session
        this.authenticatedSession = new MockHttpSession();
        this.authenticatedSession.setAttribute(CASFilter.CAS_FILTER_USER, USERNAME);
        this.basicReceipt = new CASReceipt();
        this.basicReceipt.setCasValidateUrl(CAS_VALIDATE_URL);
        this.basicReceipt.setPrimaryAuthentication(false);
        this.basicReceipt.setUserName(USERNAME);
        this.authenticatedSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, this.basicReceipt);
        
        this.mockRequest = new MockHttpServletRequest();
        this.mockResponse = new MockHttpServletResponse();
        this.filterChain = new WatchfulFilterChain();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that the filter completes basic initialization without incident.
     * @throws ServletException
     */
    public void testBasicInit() throws ServletException {
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
    }

    /**
     * Test that setting both gateway and renew results in
     * servlet exception at initialization.
     */
    public void testInitGatewayAndRenew() {
        mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.renew",
            "true");
        mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.gateway",
            "true");

        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        try {
            filter.init(mockConfig);
        } catch (ServletException e) {
            // good -- gateway and renew cannot both be set.
            return;
        }
        fail("filter should not allow both renew and gateway to be set.");
    }

    /**
     * Test that failing to set a validation URL results in 
     * servlet exception at initialization.
     */
    public void testNoValidateUrl() {
        mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.validateUrl",
            null);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        try {
            filter.init(mockConfig);
        } catch (ServletException e) {
            // good
            return;
        }
        fail("filter should fail initialization when validation URL is not set.");
    }

    /** Test that insecure validate URL results in
     * servlet exception at initialization.
     *
     * CCCI - ignored this test, due to our changes
     */
    public void ignoreTestInsecureValidateUrl(){
        mockConfig.setInitParameter(
                "edu.yale.its.tp.cas.client.filter.validateUrl",
                "http://somewhere.com/cas/serviceValidate");
            mockConfig.setupServletContext(basicContext);
            CASFilter filter = new CASFilter();
            try {
                filter.init(mockConfig);
            } catch (ServletException e) {
                // good
                return;
            }
            fail("filter should fail initialization when validation URL is insecure.");
    }
    
    /**
     * Test that the Filter fails to initialize when both serverName and serviceUrl are set,
     * as these parameters are mutually exclusive.
     */
    public void testServiceAndServerName() {
        // serverName has already been set in setup();
        mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.serviceUrl",
            "http://www.client.com:8080/login");
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        try {
            filter.init(mockConfig);
        } catch (ServletException e) {
            // good
            return;
        }
        fail("filter should fail initialization when both serverName and serviceUrl are set, as they are mutually exlusive.");
    }

    /**
     * Test that initialization fails when neither the server name nor the service URL are specified.
     *
     * CCCI - ignored this test, due to our changes
     */
    public void ignoreTestNoServerNameNoServiceUrl() {
        // serverName has already been set in setup(), but serviceUrl has not.
        mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.serverName",
            null);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        try {
            filter.init(mockConfig);
        } catch (ServletException e) {
            // good
            return;
        }
        fail("filter should fail initialization when neither serverName nor serviceUrl are set, as it will be unable to validate tickets.");
    }

    /**
     * Test that the CASFilter passes the request along the filter chain
     * when the session already includes record of authentication.
     * @throws ServletException
     * @throws IOException
     */
    public void testAlreadyAuthenticated()
        throws ServletException, IOException {

        
        assertNotNull(authenticatedSession);
        mockRequest.setSession(authenticatedSession);
        assertNotNull(mockRequest.getSession());

        WatchfulFilterChain filterChain = new WatchfulFilterChain();
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        assertTrue(filterChain.isChainInvoked());
    }
    
    /**
     * Test that the CASFilter will not accept a prior CASReceipt that does not meet its requirement
     * that the authentication be from a presentation of primary credentials -- that is, with
     * renew=true on the validation request.
     * @throws ServletException
     * @throws IOException
     */
    public void testStrictnessRequireRenew() throws ServletException, IOException{
        mockRequest.setSession(authenticatedSession);
        WatchfulFilterChain filterChain = new WatchfulFilterChain();
        mockConfig.setInitParameter("edu.yale.its.tp.cas.client.filter.renew", "true");
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        
        assertFalse(filterChain.isChainInvoked());
        assertTrue(mockResponse.wasRedirectSent());
    }
    
    /**
     * Test that the CASFilter will not accept a prior CASReceipt that does not meet its requirement
     * that the authentication not be proxied.
     * @throws ServletException
     * @throws IOException
     *
     */
    public void testStrictnessRejectProxied() throws ServletException, IOException {
        List proxyList = new ArrayList();
        proxyList.add("https://www.foo.com/proxier");
        basicReceipt.setProxyList(proxyList);
        
        mockRequest.setSession(authenticatedSession);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        
        assertFalse(filterChain.isChainInvoked());
        assertTrue(mockResponse.wasRedirectSent());
    }
    
    /**
     * Test that the CASFilter will not accept a prior CASReceipt that declares a proxying service
     * that is not among the authorized proxies.
     * @throws ServletException
     * @throws IOException
     */
    public void testStrictnessRejectUnknownProxy() throws ServletException, IOException {
        List proxyList = new ArrayList();
        proxyList.add("https://www.foo.com/proxier");
        basicReceipt.setProxyList(proxyList);
        
        mockConfig.setInitParameter(CASFilter.AUTHORIZED_PROXY_INIT_PARAM, "https://www.bar.com/proxier");
        
        mockRequest.setSession(authenticatedSession);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        
        assertFalse(filterChain.isChainInvoked());
        assertTrue(mockResponse.wasRedirectSent());
    }
    
    /** 
     * Test that the CASFilter will accept a prior CASReceipt indicating the authorized proxy.
     * @throws ServletException
     * @throws IOException
     */
    public void testAcceptProxy() throws ServletException, IOException {
        List proxyList = new ArrayList();
        proxyList.add("https://www.foo.com/proxier");
        basicReceipt.setProxyList(proxyList);
        
        mockConfig.setInitParameter(CASFilter.AUTHORIZED_PROXY_INIT_PARAM, "https://www.foo.com/proxier");
        
        mockRequest.setSession(authenticatedSession);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        
        assertTrue(filterChain.isChainInvoked());
    }
    
    /**
     * Test that the CASFilter will handle mutliple authorized proxies.
     * @throws Exception
     */
    public void testAcceptProxies() throws Exception {
        List proxyList = new ArrayList();
        proxyList.add("https://www.foo.com/proxier");
        basicReceipt.setProxyList(proxyList);
        
        mockConfig.setInitParameter(CASFilter.AUTHORIZED_PROXY_INIT_PARAM, "https://www.foo.com/proxier https://www.bar.com/proxier https://www.fred.com/proxier");
        
        mockRequest.setSession(authenticatedSession);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        
        assertTrue(filterChain.isChainInvoked());
        
        filterChain = new WatchfulFilterChain();
        proxyList.remove("https://www.foo.com/proxier");
        proxyList.add("https://www.bar.com/proxier");
        filter.doFilter(mockRequest, new MockHttpServletResponse(), filterChain);
        
        assertTrue(filterChain.isChainInvoked());
        
        filterChain = new WatchfulFilterChain();
        proxyList.remove("https://www.bar.com/proxier");
        proxyList.add("https://www.fred.com/proxier");
        filter.doFilter(mockRequest, new MockHttpServletResponse(), filterChain);
        
        assertTrue(filterChain.isChainInvoked());
        
        filterChain = new WatchfulFilterChain();
        proxyList.remove("https://www.fred.com/proxier");
        proxyList.add("https://www.wombat.com/proxier");
        filter.doFilter(mockRequest, new MockHttpServletResponse(), filterChain);
        
        assertFalse(filterChain.isChainInvoked());
    }
    
    /**
     * Test that setting an insecure proxier fails at filter initialization.
     *
     * CCCI - ignored this test, due to our changes
     */
    public void ignoreTestInsecureProxier() {
        mockConfig.setInitParameter(CASFilter.AUTHORIZED_PROXY_INIT_PARAM, "http://www.foo.com/proxier");
        
        mockRequest.setSession(authenticatedSession);
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        try {
            filter.init(mockConfig);
        } catch (ServletException e) {
            // good
            return;
        } 
        fail("Should have thrown servlet exception because of insecure entry in authorized proxy parameter");
    }
    
    /**
     * Test that the CASFilter wraps the request to implement getRemoteUser()
     * when configured to do so.
     */
    public void testGetRemoteUser() throws ServletException, IOException {
        mockConfig.setInitParameter("edu.yale.its.tp.cas.client.filter.wrapRequest", "true");
        mockConfig.setupServletContext(basicContext);
        mockRequest.setSession(authenticatedSession);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        assertEquals(USERNAME, ( (HttpServletRequest) filterChain.getFilteredServletRequest()).getRemoteUser());
    }
    
    /**
     * Test that the CASFilter does not wrap the request 
     * to implement getRemoteUser() when it is not configured to do so.
     * configured to do so.
     * @throws ServletException
     * @throws IOException
     */
    public void testNotRemoteUser() throws ServletException, IOException {
        mockConfig.setupServletContext(basicContext);
        mockRequest.setSession(authenticatedSession);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        assertNull(( (HttpServletRequest) filterChain.getFilteredServletRequest()).getRemoteUser());
    }

    /**
     * Test that the filter will redirect to the CAS Login URL.
     */
    public void testRedirect() throws ServletException, IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setSession(new MockHttpSession());
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);
        filter.doFilter(mockRequest, mockResponse, filterChain);
        assertFalse(filterChain.isChainInvoked());
        assertTrue(mockResponse.wasRedirectSent());
    }

    /**
     * Test that when gateway is true, requests simulating the redirect back from CAS with no ticket
     * are passed through the chain.
     * @throws ServletException
     * @throws IOException
     */
    public void testGateway() throws ServletException, IOException {
        // configure the filter gateway feature
        mockConfig.setInitParameter(
            "edu.yale.its.tp.cas.client.filter.gateway",
            "true");
        mockConfig.setupServletContext(basicContext);
        CASFilter filter = new CASFilter();
        filter.init(mockConfig);

        // construct an initial request, with no ticket.

        MockHttpSession bareSession = new MockHttpSession();
        MockHttpServletRequest bareRequest = new MockHttpServletRequest();
        bareRequest.setSession(bareSession);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        filter.doFilter(bareRequest, mockResponse, filterChain);

        // the filter should have redirected to CAS with gateway = true
        assertFalse(filterChain.isChainInvoked());
        assertTrue(mockResponse.wasRedirectSent());

        filterChain = null;
        bareRequest = null;

        // construct a second request in the same session, still no ticket.

        WatchfulFilterChain secondChain = new WatchfulFilterChain();
        MockHttpServletRequest secondRequest = new MockHttpServletRequest();
        secondRequest.setSession(bareSession);
        mockResponse = new MockHttpServletResponse();
        filter.doFilter(secondRequest, mockResponse, secondChain);

        // the filter should have gatewayed this right through.

        assertTrue(secondChain.isChainInvoked());
        assertFalse(mockResponse.wasRedirectSent());

        // construct a third request in the same session, still no ticket.
        
        WatchfulFilterChain thirdChain = new WatchfulFilterChain();
        MockHttpServletRequest thirdRequest = new MockHttpServletRequest();
        thirdRequest.setSession(bareSession);
        mockResponse = new MockHttpServletResponse();
        filter.doFilter(thirdRequest, mockResponse, thirdChain);
        
        // the filter should have gatewayed this right through.
        
        assertTrue(secondChain.isChainInvoked());
        assertFalse(mockResponse.wasRedirectSent());
    }

}

/* CASFilterTest.java
 * 
 * Copyright (c) Jun 22, 2004 Yale University.  All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 * DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms,
 * with or without modification, are permitted, provided that the
 * following conditions are met.
 * 
 * 1. Any redistribution must include the above copyright notice and
 * disclaimer and this list of conditions in any related documentation
 * and, if feasible, in the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product
 * includes software developed by Yale University," in any related
 * documentation and, if feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse
 * or promote products derived from this software.
 */