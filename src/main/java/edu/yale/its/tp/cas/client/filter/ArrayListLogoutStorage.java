package edu.yale.its.tp.cas.client.filter;

import java.util.ArrayList;
import java.util.List;

public class ArrayListLogoutStorage implements LogoutStorage {

    List<String> tickets = new ArrayList<String>();

    @Override
    public boolean contains(String ticket) {
        return tickets.contains(ticket);
    }

    @Override
    public void add(String ticket) {
        tickets.add(ticket);
    }
}
