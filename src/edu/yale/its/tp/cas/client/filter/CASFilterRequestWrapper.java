/*
 *  Copyright (c) 2000-2003 Yale University. All rights reserved.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY
 *  DISCLAIMED. IN NO EVENT SHALL YALE UNIVERSITY OR ITS EMPLOYEES BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED, THE COSTS OF
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH
 *  DAMAGE.
 *
 *  Redistribution and use of this software in source or binary forms,
 *  with or without modification, are permitted, provided that the
 *  following conditions are met:
 *
 *  1. Any redistribution must include the above copyright notice and
 *  disclaimer and this list of conditions in any related documentation
 *  and, if feasible, in the redistributed software.
 *
 *  2. Any redistribution must include the acknowledgment, "This product
 *  includes software developed by Yale University," in any related
 *  documentation and, if feasible, in the redistributed software.
 *
 *  3. The names "Yale" and "Yale University" must not be used to endorse
 *  or promote products derived from this software.
 */

package edu.yale.its.tp.cas.client.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.cas.client.CASReceipt;

/**
 * <p>
 * Wraps the <code>HttpServletRequest</code> object, replacing
 * <code>getRemoteUser()</code> with a version that returns the current CAS
 * logged-in user.
 * </p>
 * 
 * @author Drew Mazurek
 */
public class CASFilterRequestWrapper extends HttpServletRequestWrapper
{

    private static Log log = LogFactory.getLog(CASFilterRequestWrapper.class);

    // CCCI
    private String remoteUserAttrib;
    
    @Override
    public HttpSession getSession()
    {
        // TODO Auto-generated method stub
        return new CASFilterHttpSessionWrapper(super.getSession());
    }
    
    @Override
    public HttpSession getSession(boolean create)
    {
        HttpSession session = super.getSession(create);
        if(session==null) return null;
        return new CASFilterHttpSessionWrapper(session);
    }

    public CASFilterRequestWrapper(HttpServletRequest request, String remoteUserAttrib)
    {
        super(request);
        if (log.isTraceEnabled())
        {
            log.trace("wrapping an HttpServletRequest in a CASFilterRequestWrapper.");
        }
        this.remoteUserAttrib = remoteUserAttrib;
    }

    @Override
    public String getHeader(String name)
    {
        if (name.equals(CASFilter.CAS_FILTER_USER)) return getRemoteUser();
        if (name.equals("CAS_USER")) return getRemoteUser();

        if (name.startsWith("CAS_"))
        {
            CASReceipt receipt = (CASReceipt) getSession().getAttribute(CASFilter.CAS_FILTER_RECEIPT);
            if(receipt==null) return null;
            return (String) receipt.getAttributes().get(name.substring(4));
        }

        return super.getHeader(name);
    }

    @Override
    public Enumeration getHeaderNames()
    {
        ArrayList a = new ArrayList();
        Enumeration e = super.getHeaderNames();
        while (e.hasMoreElements())
        {
            a.add(e.nextElement());
        }
        a.add(CASFilter.CAS_FILTER_USER);
        a.add("CAS_USER");

        CASReceipt receipt = (CASReceipt) getSession().getAttribute(CASFilter.CAS_FILTER_RECEIPT);
        if(receipt!=null)
        {
            for (Object name : receipt.getAttributes().keySet())
            {
                a.add("CAS_" + name);
            }
        }
        
        return Collections.enumeration(a);
    }

    @Override
    public Enumeration getHeaders(String name)
    {
        if (name.equals(CASFilter.CAS_FILTER_USER) || name.equals("CAS_USER"))
        {
            ArrayList a = new ArrayList();
            a.add(getRemoteUser());
            return Collections.enumeration(a);
        }

        if (name.startsWith("CAS_"))
        {
            CASReceipt receipt = (CASReceipt) getSession().getAttribute(CASFilter.CAS_FILTER_RECEIPT);
            if(receipt!=null)
            {
                ArrayList a = new ArrayList();
                a.add(receipt.getAttributes().get(name.substring(4)));
                return Collections.enumeration(a);
            }
        }

        return super.getHeaders(name);
    }

    /**
     * <p>
     * Returns the currently logged in CAS user.
     * </p>
     * <p>
     * Specifically, this returns the value of the session attribute,
     * <code>CASFilter.CAS_FILTER_USER</code>.
     * </p>
     */
    public String getRemoteUser()
    {
        String user = null;
        if (remoteUserAttrib != null && remoteUserAttrib.trim().length() > 0)
        {
            CASReceipt receipt = (CASReceipt) getSession().getAttribute(CASFilter.CAS_FILTER_RECEIPT);
            if(receipt!=null)
            {
                user = (String) receipt.getAttributes().get(remoteUserAttrib);
            }
        }
        else
        {
            user = (String) getSession().getAttribute(CASFilter.CAS_FILTER_USER);
        }
        if (log.isTraceEnabled())
        {
            log.trace("getRemoteUser() returning [" + user + "]");
        }
        return user;
    }
}
