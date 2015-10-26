<%@ page import="uk.ac.ebi.rcloud.version.SoftwareVersion" %>
<% response.setContentType("text/xml"); %>

<!--?xml version="1.0" encoding="UTF-8" standalone="no"?-->
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>test xml</comment>
<entry key="version"><%=SoftwareVersion.getVersion()%></entry>
</properties>
