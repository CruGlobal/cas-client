package edu.yale.its.tp.cas.client.filter;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class CASFilterHttpSessionWrapper implements HttpSession
{

    HttpSession wrappedSession;
    
    
    public CASFilterHttpSessionWrapper(HttpSession wrappedSession)
    {
        super();
        this.wrappedSession = wrappedSession;
    }


    public Object getAttribute(String arg0)
    {
        return wrappedSession.getAttribute(arg0);
    }

    public Enumeration getAttributeNames()
    {
        return wrappedSession.getAttributeNames();
    }

    public long getCreationTime()
    {
        return wrappedSession.getCreationTime();
    }

    public String getId()
    {
        return wrappedSession.getId();
    }

    public long getLastAccessedTime()
    {
        return wrappedSession.getLastAccessedTime();
    }

    public int getMaxInactiveInterval()
    {
        return wrappedSession.getMaxInactiveInterval();
    }

    public ServletContext getServletContext()
    {
        return wrappedSession.getServletContext();
    }

    public HttpSessionContext getSessionContext()
    {
        return wrappedSession.getSessionContext();
    }

    public Object getValue(String arg0)
    {
        return wrappedSession.getValue(arg0);
    }

    public String[] getValueNames()
    {
        return wrappedSession.getValueNames();
    }

    public boolean isNew()
    {
        return wrappedSession.isNew();
    }

    public void setMaxInactiveInterval(int arg0)
    {
        wrappedSession.setMaxInactiveInterval(arg0);
    }

    public void invalidate()
    {
        //wrappedSession.invalidate();
    }

    public void putValue(String arg0, Object arg1)
    {
        if(arg0!=null && arg0.startsWith("edu.yale")) return;
        wrappedSession.putValue(arg0, arg1);
    }

    public void removeAttribute(String arg0)
    {
        if(arg0!=null && arg0.startsWith("edu.yale")) return;
        wrappedSession.removeAttribute(arg0);
    }

    public void removeValue(String arg0)
    {
        if(arg0!=null && arg0.startsWith("edu.yale")) return;
        wrappedSession.removeValue(arg0);
    }

    public void setAttribute(String arg0, Object arg1)
    {
        if(arg0!=null && arg0.startsWith("edu.yale")) return;
        wrappedSession.setAttribute(arg0, arg1);
    }

}
