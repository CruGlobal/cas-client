package edu.yale.its.tp.cas.client.filter;

public interface LogoutStorage {

    public boolean contains(String ticket);
    public void add(String ticket);
}
