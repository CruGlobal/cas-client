/*
 * Created on Jun 27, 2004
 *
 * Copyright(c) Yale University, Jun 27, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.client;

import javax.servlet.ServletException;

import com.mockrunner.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * JUnit testcase for the CAS client utilities class.
 * @author andrew.petro@yale.edu
 */
public class UtilTest extends TestCase {

    private static final String requestServerName = "requestProvided.com";
    private static final String requestUri = "/app/servlet";
    private static final String serverName = "someplace.com";
    /**
     * Constructor for UtilTest.
     * @param name
     */
    public UtilTest(String name) {
        super(name);
    }

    /**
     * Basic test for getService.
     * Demonstrates that getService does not use the serverName provided in the Request.
     * Demonstrates that getService returns a URL-encoded response.
     * Demonstrates that getService removes the 'ticket' parameter.
     * @throws ServletException
     */
    public void testGetServiceBasics() throws ServletException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setQueryString("param=value&ticket=splat");
        mockRequest.setRequestURI(requestUri);
        mockRequest.setServerName(requestServerName);
        String response = Util.getService(mockRequest, serverName);
        
        
        // try both uppercase and lowercase since some implementations of URLEncoder
        // use uppercase and some use lowercase.
        String expectedResponseCaps =
            "http%3A%2F%2Fsomeplace.com%2Fapp%2Fservlet%3Fparam%3Dvalue";
        String expectedResponseLowercase = "http%3a%2f%2fsomeplace.com%2fapp%2fservlet%3fparam%3dvalue";
        
        
        boolean passed = expectedResponseCaps.equals(response);
        passed = passed || expectedResponseLowercase.equals(response);
        
        assertTrue(passed);
    }

    /**
     * Test that getService retains the order of the parameters and values in the query string.
     * Demonstrates getService behaviour in absence of the 'ticket' parameter.
     * This test is failed by Mik Lernout's otherwise meritorious suggestion for changes
     * as of June 2004.
     * @throws ServletException
     */
    public void testGetServiceOddButLegalQueryString() throws ServletException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setQueryString("param1=aaa&param2=bbb&param1=aaa");
        mockRequest.setRequestURI(requestUri);
        mockRequest.setServerName(requestServerName);
        String response = Util.getService(mockRequest, serverName);
        String expectedResponseCaps =
            "http%3A%2F%2Fsomeplace.com%2Fapp%2Fservlet%3Fparam1%3Daaa%26param2%3Dbbb%26param1%3Daaa";
        
        String expectedResponseLowercase = 
            "http%3a%2f%2fsomeplace.com%2fapp%2fservlet%3fparam1%3daaa%26param2%3dbbb%26param1%3daaa";
        boolean passed = expectedResponseCaps.equals(response);
        passed = passed || expectedResponseLowercase.equals(response);
        assertTrue(passed);
    }

}

/* UtilTest.java
 * 
 * Copyright (c) Jun 27, 2004 Yale University.  All rights reserved.
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