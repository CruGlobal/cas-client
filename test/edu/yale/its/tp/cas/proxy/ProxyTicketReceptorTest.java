/*
 * Created on Jul 18, 2004
 *
 * Copyright(c) Yale University, Jul 18, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.proxy;

import java.io.IOException;

import javax.servlet.ServletException;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;

import junit.framework.TestCase;

/**
 * Testcase for the ProxyTicketReceptor servlet.
 * This testcase should generate no network traffic.
 * 
 * @author andrew.petro@yale.edu
 */
public class ProxyTicketReceptorTest extends TestCase {

    /**
     * Name of the servlet initialization parameter that ProxyTicketReceptor
     * expects to contain the https: URL of the CAS proxy ticket vending
     * service. We don't use the constant exposed by ProxyTicketReceptor because
     * this value is part of the public deployer interface exposed by
     * ProxyTicketReceptor.
     */
    private static final String CASPROXYURL_INIT_PARAM_NAME = "edu.yale.its.tp.cas.proxyUrl";

    /**
     * A new ServletConfig, ready to receive the (potentially modified)
     * basicContext.
     */
    private MockServletConfig basicConfig;

    private MockServletContext basicContext;

    private MockHttpServletRequest mockRequest;

    private MockHttpServletResponse mockResponse;

    /**
     * The servlet to being tested.
     */
    private ProxyTicketReceptor proxyTicketReceptor;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.basicConfig = new MockServletConfig();
        this.basicConfig.setInitParameter(CASPROXYURL_INIT_PARAM_NAME,
                "https://someplace.edu/cas/proxy");
        this.mockRequest = new MockHttpServletRequest();
        this.mockResponse = new MockHttpServletResponse();
        this.basicContext = new MockServletContext();
        this.basicConfig.setServletContext(this.basicContext);
        this.proxyTicketReceptor = new ProxyTicketReceptor();
    }

    /**
     * Test that servlet initialization fails when the configuration does
     * include the URL where CAS offers its ProxyTicket vending servlet.
     */
    public void testNoCasProxyUrlInit() {
        this.basicConfig.setInitParameter(CASPROXYURL_INIT_PARAM_NAME, null);
        try {
            this.proxyTicketReceptor.init(this.basicConfig);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have thrown ServletException because casProxyUrl was not set.");
    }

    /**
     * Test basic servlet initialization, with the proxy ticket receptor URL set
     * in a servlet initialization parameter.
     * 
     * @throws ServletException
     */
    public void testBasicInit() throws ServletException {
        this.proxyTicketReceptor.init(this.basicConfig);
    }

    /**
     * Test basic servlet initialization, with the proxy ticket receptor URL set
     * in the servlet context initialization parameter (not in the servlet initialization parameter).
     * @throws ServletException
     */
    public void testContextProxyUrlInit() throws ServletException {
        this.basicConfig.setInitParameter(CASPROXYURL_INIT_PARAM_NAME, null);
        this.basicContext.setInitParameter(CASPROXYURL_INIT_PARAM_NAME,
                "https://someplace.com/cas/proxy");

        this.proxyTicketReceptor.init(this.basicConfig);
    }

    /**
     * Test that servlet initialization fails when cas proxy URL is not an https: URL.
     */
    public void testInsecureProxyUrlInit() {
        this.basicConfig.setInitParameter(CASPROXYURL_INIT_PARAM_NAME, "http://www.insecure.com/cas/proxy");
        try {
            this.proxyTicketReceptor.init(this.basicConfig);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have thrown ServletException because casProxyUrl was not an https: URL.");
    }
    
    /**
     * Test that servlet initialization fails when cas proxy URL is not an https: URL,
     * as set in servlet context initialization rather than servlet initialization.
     */
    public void testContextInsecureProxyUrlInit() {
        this.basicConfig.setInitParameter(CASPROXYURL_INIT_PARAM_NAME, null);
        this.basicContext.setInitParameter(CASPROXYURL_INIT_PARAM_NAME, "http://www.insecure.com/cas/proxy");
        try {
            this.proxyTicketReceptor.init(this.basicConfig);
        } catch (ServletException e) {
            // correct
            return;
        }
        fail("Initialization should have thrown ServletException because casProxyUrl was not an https: URL.:" + this.proxyTicketReceptor);
    }
    
    /**
     * Test that the receptor successfully receives tickets without throwing an exception.
     */
    public void testReceiveProxyTicket() throws ServletException, IOException {
        this.proxyTicketReceptor.init(this.basicConfig);
        this.mockRequest.setRequestURL("https://someplace.com/app/casProxyReceptor?pgtIou=FOO&pgtId=BAR");
        this.proxyTicketReceptor.doGet(this.mockRequest, this.mockResponse);
        // TODO: test for proper response
    }

    /**
     * Test that the receptor returns null on a request with an unknown pgtIou.
     * @throws ServletException
     * @throws IOException
     */
    public void testGetUnknownProxyTicket() throws ServletException, IOException {
        this.proxyTicketReceptor.init(this.basicConfig);
        assertNull(ProxyTicketReceptor.getProxyTicket("SPLAT", "http://www.nowhere.com/someService"));
    }

}

/*
 * ProxyTicketReceptorTest.java
 * 
 * Copyright (c) Jul 18, 2004 Yale University. All rights reserved.
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