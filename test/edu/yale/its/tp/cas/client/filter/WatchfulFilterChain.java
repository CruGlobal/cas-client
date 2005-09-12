/*
 * Created on Jun 28, 2004
 *
 * Copyright(c) Yale University, Jun 28, 2004.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.cas.client.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A test filter chain which merely records whether and with what arguments its
 * doFilter() method was invoked.
 * @author andrew.petro@yale.edu
 */
public class WatchfulFilterChain implements FilterChain {
    private boolean chainInvoked = false;
    private ServletRequest filteredServletRequest;
    private ServletResponse filteredServletResponse;
    
    /**
     * Has the doFilter method of this test chain been invoked
     * @return true if invoked, false otherwise.
     */
    public boolean isChainInvoked(){
        return chainInvoked;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void doFilter(ServletRequest arg0, ServletResponse arg1)
        throws IOException, ServletException {
        this.chainInvoked = true;
        this.filteredServletRequest = arg0;
        this.filteredServletResponse = arg1;
    }

    /**
     * Get the request argument from previous doFilter() invocation.
     * @return most recent request, or null if not invoked.
     */
    public ServletRequest getFilteredServletRequest() {
        return filteredServletRequest;
    }

    /**
     * Get the response argument from previous doFilter() invocation.
     * @return most recent response, or null if not invoked.
     */
    public ServletResponse getFilteredServletResponse() {
        return filteredServletResponse;
    }

}


/* FailFilterChain.java
 * 
 * Copyright (c) Jun 28, 2004 Yale University.  All rights reserved.
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