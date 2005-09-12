/*
 * Created on Oct 1, 2004
 */
package edu.yale.its.tp.cas.util;

/**
 * @author Nathan.Kopp
 */
public class TimeoutThread extends Thread
{
    public Exception e;
    public Object retVal;
    Object[] params;
    public synchronized void cleanup() {}
    public synchronized Object startWithTimeout(Object[] params, int timeout) throws Exception
    {
        this.params = params;
        start();
        try
        {
            wait(timeout);
            cleanup();
        }
        catch (InterruptedException e) { /* do nothing */ }
        if(e!=null) throw new Exception(e);
        return retVal;
    }
    public void run()
    {
        try
        {
            this.retVal = run(params);
        }
        catch (Exception e)
        {
            this.e = e;
        }
    }
    public synchronized Object run(Object[] params) throws Exception { return null; }
}
