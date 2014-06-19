/*
 * Created on Aug 17, 2004
 *
 * Copyright(c) Yale University, Aug 17, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.client.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

import com.mockrunner.mock.web.MockFilterConfig;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletContext;

import edu.yale.its.tp.cas.client.CASReceipt;

import junit.framework.TestCase;

/**
 * Test the ProxyChainScrutinizerFilter.
 * 
 * @author andrew.petro@yale.edu
 * @version $Revision: 1.1 $ $Date: 2004/08/18 03:58:21 $
 */
public class ProxyChainScrutinizerFilterTest extends TestCase {
    /*
     * @see TestCase#setUp()
     */
    private static final String GOOD_AUTHORIZED_PROXIES_STRING = "https://www.yale.edu/immediatelyPreviousServiceInChain https://www.princeton.edu/middleware https://www.northwestern.edu/userInitiallyAuthenticatedToThisApplication"
            + " ; https://www.immediatelyPreviousAuthenticationProxyingService.com https://secure.com/middleTierService https://secure.com/userInterface";

    private ProxyChainScrutinizerFilter correctlyConfiguredFilter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private WatchfulFilterChain filterChain;

    protected void setUp() throws Exception {
        super.setUp();
        MockFilterConfig config = new MockFilterConfig();
        MockServletContext context = new MockServletContext();
        config.setInitParameter(
                ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM,
                GOOD_AUTHORIZED_PROXIES_STRING);
        config.setupServletContext(context);
        this.correctlyConfiguredFilter = new ProxyChainScrutinizerFilter();
        this.correctlyConfiguredFilter.init(config);
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.filterChain = new WatchfulFilterChain();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that filter initialization fails when the authorized proxy chains
     * parameter is not set.
     */
    public void testInitNoParam() {
        MockFilterConfig config = new MockFilterConfig();
        MockServletContext context = new MockServletContext();
        config.setupServletContext(context);
        ProxyChainScrutinizerFilter filter = new ProxyChainScrutinizerFilter();
        try {
            filter.init(config);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have failed because "
                + ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM
                + " was not set.");
    }

    /**
     * Test that filter initialization fails for garbage values of parameter.
     */
    public void testEgregiouslyBadParam() {
        MockFilterConfig config = new MockFilterConfig();
        MockServletContext context = new MockServletContext();
        context.setInitParameter(
                ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM,
                "wombat foobar splat");
        config.setupServletContext(context);
        ProxyChainScrutinizerFilter filter = new ProxyChainScrutinizerFilter();
        try {
            filter.init(config);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have failed because "
                + ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM
                + " had garbage values.");
    }

    /**
     * Test that filter initialization fails when http:// (not https://)
     * services are named in the supposedly authorized proxy chains.
     */
    public void testInsecureUrlsParam() {
        MockFilterConfig config = new MockFilterConfig();
        MockServletContext context = new MockServletContext();
        String badAuthorizedProxyChains = "https://www.yale.edu/bulldog https://www.princeton.edu/tiger https://www.northwestern.edu/wildcat"
                + " ; https://www.beginSecondChain.com http://insecure.com/service https://secure.com/anotherService";
        context.setInitParameter(
                ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM,
                badAuthorizedProxyChains);
        config.setupServletContext(context);
        ProxyChainScrutinizerFilter filter = new ProxyChainScrutinizerFilter();
        try {
            filter.init(config);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have failed because "
                + ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM
                + " contained an insecure URL.");
    }

    /**
     * Test that filter initialization fails for empty parameter.
     */
    public void testEmptyParam() {
        MockFilterConfig config = new MockFilterConfig();
        MockServletContext context = new MockServletContext();
        context.setInitParameter(
                ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM,
                "     ");
        config.setupServletContext(context);
        ProxyChainScrutinizerFilter filter = new ProxyChainScrutinizerFilter();
        try {
            filter.init(config);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have failed because "
                + ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM
                + " had only whitespace");
    }

    /**
     * Test that filter initialization succeeds for a legitimate value. If this
     * test were to fail, all tests would fail because this logic is used in
     * setUp() to enable the tests of doFilter.
     * 
     * @throws ServletException
     */
    public void testGoodParam() throws ServletException {
        MockFilterConfig config = new MockFilterConfig();
        MockServletContext context = new MockServletContext();
        config.setInitParameter(
                ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM,
                GOOD_AUTHORIZED_PROXIES_STRING);
        config.setupServletContext(context);
        ProxyChainScrutinizerFilter filter = new ProxyChainScrutinizerFilter();
        filter.init(config);
    }

    /**
     * Test that doFilter() throws ServletException when request is not an
     * HttpServletRequest and response is not an HttpServletResponse.
     * 
     * @throws IOException
     */
    public void testDoFilterNotHttpServletRequestNotHttpServletResponse() throws IOException {
        ServletRequestWrapper notHttpRequest = new ServletRequestWrapper(
                this.request);
        ServletResponseWrapper notHttpResponse = new ServletResponseWrapper(
                this.response);

        try {
            this.correctlyConfiguredFilter.doFilter(notHttpRequest,
                    notHttpResponse, this.filterChain);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("doFilter() should have failed because request was not an HttpServletRequest.");
    }

    /**
     * Test that doFilter() sends response FORBIDDEN when request is not an
     * HttpServletRequest but response is an HttpServletResponse.
     * @throws IOException
     * @throws ServletException
     */
    public void testDoFilterNotHttpServletRequestButHttpServletResponse() throws IOException, ServletException {
        ServletRequestWrapper notHttpRequest = new ServletRequestWrapper(
                this.request);

        this.correctlyConfiguredFilter.doFilter(notHttpRequest, this.response,
                this.filterChain);
        
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that doFilter() sends response FORBIDDEN when there is no session into which
     * might have been stored a CASReceipt.
     * @throws IOException
     * @throws ServletException
     */
    public void testDoFilterNoSession() throws IOException, ServletException {

        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that doFilter() sends a response FORBIDDEN when there is a session but the 
     * CASReceipt attribute is not set.
     * @throws IOException
     * @throws ServletException
     */
    public void testDoFilterSessionButNoAttribute() throws IOException, ServletException {
        this.request.setSession(new MockHttpSession());
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }

    /**
     * Test that doFilter() sends a response FORBIDDEN when there is a session and the 
     * CASFilter request attribute is set but its value is not of type CASReceipt.
     * @throws ServletException
     * @throws IOException
     *
     */
    public void testDoFilterSessionAttributeOfWrongType() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, new ProxyChainScrutinizerFilter());
        this.request.setSession(mockSession);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that an authorized proxy chain gets through the filter.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterAuthorizedProxyChainOne() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        CASReceipt receipt = new CASReceipt();
        List proxyList = new ArrayList();
        proxyList.add("https://www.yale.edu/immediatelyPreviousServiceInChain");
        proxyList.add("https://www.princeton.edu/middleware");
        proxyList.add("https://www.northwestern.edu/userInitiallyAuthenticatedToThisApplication");
        receipt.setProxyList(proxyList);
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, receipt);
        this.request.setSession(mockSession);
        this.request.getSession(true);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertTrue(this.filterChain.isChainInvoked());
    }
    
    /**
     * Test that an authorized proxy chain gets through the filter.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterAuthorizedProxyChainTwo() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        CASReceipt receipt = new CASReceipt();
        List proxyList = new ArrayList();
        proxyList.add("https://www.immediatelyPreviousAuthenticationProxyingService.com");
        proxyList.add("https://secure.com/middleTierService");
        proxyList.add("https://secure.com/userInterface");
        receipt.setProxyList(proxyList);
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, receipt);
        this.request.setSession(mockSession);
        this.request.getSession(true);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertTrue(this.filterChain.isChainInvoked());
    }
    
    /**
     * Test that an unauthorized proxy chain similar to an authorized chain but for one change
     * prompts a FORBIDDEN.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterUnauthorizedProxyChain() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        CASReceipt receipt = new CASReceipt();
        List proxyList = new ArrayList();
        proxyList.add("https://www.immediatelyPreviousAuthenticationProxyingService.com");
        proxyList.add("https://secure.com/middleTierService");
        proxyList.add("https://secure.com/notTheAuthorizedUserInterface");
        receipt.setProxyList(proxyList);
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, receipt);
        this.request.setSession(mockSession);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that a prefix of an authorized proxy chain does not get through the filter.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterPrefixOfAuthorizedChain() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        CASReceipt receipt = new CASReceipt();
        List proxyList = new ArrayList();
        proxyList.add("https://www.immediatelyPreviousAuthenticationProxyingService.com");
        proxyList.add("https://secure.com/middleTierService");
        receipt.setProxyList(proxyList);
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, receipt);
        this.request.setSession(mockSession);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that a suffix of an authorized chain prompts a FORBIDDEN.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterSuffixOfAuthorizedChain() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        CASReceipt receipt = new CASReceipt();
        List proxyList = new ArrayList();
        proxyList.add("https://www.princeton.edu/middleware");
        proxyList.add("https://www.northwestern.edu/userInitiallyAuthenticatedToThisApplication");
        receipt.setProxyList(proxyList);
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, receipt);
        this.request.setSession(mockSession);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that a Service Ticket receipt prompts a FORBIDDEN when the filter is configured to do so.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterServiceTicketReceipt() throws IOException, ServletException {
        MockHttpSession mockSession = new MockHttpSession();
        CASReceipt receipt = new CASReceipt();
        mockSession.setAttribute(CASFilter.CAS_FILTER_RECEIPT, receipt);
        this.request.setSession(mockSession);
        this.correctlyConfiguredFilter.doFilter(this.request, this.response,
                this.filterChain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, this.response.getErrorCode());
    }
    
    /**
     * Test that a request in a session including a CASReceipt representing
     * a previously validated service ticket does get through the filter
     * when configured to accept service tickets.
     * @throws ServletException
     * @throws IOException
     */
    public void testDoFilterAuthorizedServiceTicketReceipt() throws IOException, ServletException {
        /* notice the leading semicolon.
         * This configures the filter to accept the empty list proxy chain 
         * which is the proxy chain for service ticket receipts.
         */ 
        String authorizedProxiesInitParam = "; https://www.yale.edu/immediatelyPreviousServiceInChain https://www.princeton.edu/middleware https://www.northwestern.edu/userInitiallyAuthenticatedToThisApplication"
        + " ; https://www.immediatelyPreviousAuthenticationProxyingService.com https://secure.com/middleTierService https://secure.com/userInterface";

        MockServletContext context = new MockServletContext();
        MockFilterConfig config = new MockFilterConfig();
        config.setInitParameter(ProxyChainScrutinizerFilter.AUTHORIZED_PROXIES_INITPARAM, authorizedProxiesInitParam);
        config.setupServletContext(context);
        ProxyChainScrutinizerFilter localFilter = new ProxyChainScrutinizerFilter();
        localFilter.init(config);
        
        CASReceipt serviceTicketReceipt = new CASReceipt();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CASFilter.CAS_FILTER_RECEIPT, serviceTicketReceipt);
        
        this.request.setSession(session);

        this.request.getSession(true);
        
        localFilter.doFilter(this.request, this.response, this.filterChain);
        assertTrue(this.filterChain.isChainInvoked());
        
    }
    
}

/*
 * ProxyChainScrutinizerFilterTest.java
 * 
 * Copyright (c) Aug 17, 2004 Yale University. All rights reserved.
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
 * are met.
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