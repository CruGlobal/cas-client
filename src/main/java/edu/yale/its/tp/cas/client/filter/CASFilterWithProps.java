package edu.yale.its.tp.cas.client.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

public class CASFilterWithProps extends CASFilter {

    Set<String> casFilterInitParameterNames =
            Sets.newHashSet(LOGIN_INIT_PARAM,
                    VALIDATE_INIT_PARAM,
                    SERVICE_INIT_PARAM,
                    AUTHORIZED_PROXY_INIT_PARAM,
                    RENEW_INIT_PARAM,
                    SERVERNAME_INIT_PARAM,
                    PROXY_CALLBACK_INIT_PARAM,
                    LOGOUT_CALLBACK_INIT_PARAM,
                    WRAP_REQUESTS_INIT_PARAM,
                    GATEWAY_INIT_PARAM,
                    REMOTE_USER_ATTRIB_INIT_PARAM,
                    URL_PATTERN_EXCLUDE_INIT_PARAM);

    @Inject
    @CasFilterProperties
    private Properties properties;

    @Override
    public void init(final FilterConfig webXmlConfig) throws ServletException {
        final HashMap<String, String> filterInitParameters = Maps.newHashMap();

        for (String initParameterName : Collections.list(webXmlConfig.getInitParameterNames())) {
            filterInitParameters.put(initParameterName, webXmlConfig.getInitParameter(initParameterName));
        }

        for (String initParameterName : casFilterInitParameterNames) {
            if (!Strings.isNullOrEmpty(properties.getProperty(initParameterName)))
                filterInitParameters.put(initParameterName, properties.getProperty(initParameterName));
        }

        FilterConfig filterConfig = new FilterConfig() {
            @Override
            public String getFilterName() {
                return webXmlConfig.getFilterName();
            }

            @Override
            public ServletContext getServletContext() {
                return webXmlConfig.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return filterInitParameters.get(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(filterInitParameters.keySet());
            }
        };

        super.init(filterConfig);
    }
}
