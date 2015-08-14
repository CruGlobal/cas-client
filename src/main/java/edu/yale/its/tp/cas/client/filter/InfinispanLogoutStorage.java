package edu.yale.its.tp.cas.client.filter;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * @author Matt Drees
 */
public class InfinispanLogoutStorage implements LogoutStorage
{

    private final Cache<String, Boolean> cache;

    public InfinispanLogoutStorage(Object storage)
    {
        CacheContainer cacheContainer = (CacheContainer) storage;
        cache = cacheContainer.getCache();
    }

    @Override
    public boolean contains(String ticket)
    {
        return cache.containsKey(ticket);
    }

    @Override
    public void add(String ticket)
    {
        cache.put(ticket, true);
    }
}
