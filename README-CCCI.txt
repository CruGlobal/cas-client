  <filter>
    <filter-name>CAS Filter</filter-name>
    <filter-class>edu.yale.its.tp.cas.client.filter.CASFilter</filter-class>
    <init-param>
      <param-name>edu.yale.its.tp.cas.client.filter.loginUrl</param-name>
      <param-value>https://give.ccci.org/login</param-value>
    </init-param>
    <init-param>
      <param-name>edu.yale.its.tp.cas.client.filter.validateUrl</param-name>
      <param-value>https://signin.mygcx.org/cas/proxyValidate</param-value>
    </init-param>
    <init-param>
      <param-name>edu.yale.its.tp.cas.client.filter.serverName</param-name>
      <param-value>give.ccci.org</param-value>
    </init-param>

    <init-param>
      <param-name>edu.yale.its.tp.cas.client.filter.logoutCallbackUrl</param-name>
      <param-value>https://give.ccci.org/logout</param-value>
    </init-param>

  </filter>

  <filter-mapping>
    <filter-name>CAS Filter</filter-name>
    <url-pattern>/casified/*</url-pattern>
  </filter-mapping>
  
  
  <filter>
  	<filter-name>Logout Echo Filter</filter-name>
  	<filter-class>edu.yale.its.tp.cas.client.util.LogoutEchoFilter</filter-class>
  	<init-param>
  		<param-name>edu.yale.its.tp.cas.logout.echo.targets</param-name>
  		<param-value>http://hart-a041.net.ccci.org:8280/logout</param-value>
  	</init-param>
  </filter>
  
  <filter-mapping>
    <filter-name>Logout Echo Filter</filter-name>
    <url-pattern>/logout/*</url-pattern>
  </filter-mapping>
  <filter-mapping>
    <filter-name>Logout Echo Filter</filter-name>
    <url-pattern>/logout</url-pattern>
  </filter-mapping>  