/*  Copyright (c) 2000-2004 Yale University. All rights reserved. 
 *  See full notice at end.
 */

package edu.yale.its.tp.cas.client.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutTrapFilter implements Filter
{

    public void init(FilterConfig config) throws ServletException
    {
    }

    // *********************************************************************
    // Filter processing

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws ServletException,
            IOException
    {
        String queryString = ((HttpServletRequest)request).getQueryString();
        if(queryString!=null && queryString.contains("cmd=logout"))
        {
            for(Cookie c : ((HttpServletRequest)request).getCookies())
            {
                if(c.getName().contains("PORTAL-PSJSESSIONID"))
                {
                    c.setMaxAge(0);
                    ((HttpServletResponse) response).addCookie(c);
                }
            }
            ((HttpServletResponse) response).sendRedirect("https://signin.ccci.org/cas/logout");
        }
        else
        {
            fc.doFilter(request, response);
        }
    }
    
    @Override
    public void destroy()
    {
    }
}