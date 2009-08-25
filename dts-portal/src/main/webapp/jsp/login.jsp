<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html PUBLIC
  "-//W3C//DTD XHTML 1.1 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>Data Transfer Service Login Page</title>
  <s:head/>
</head>
<body>
  <h3>DTS Login</h3>
  <ol>
    <li>Upload your myproxy credentials using <a href="http://grix.arcs.org.au/downloads/webstart/grix.jnlp">
        grix webstart</a>.
    </li><br/>
    <li>If grix webstart didn't work, try using the <a href="http://grix.arcs.org.au/downloads/index.html">grix binary</a>.</li><br/>
    <li>Then login using your myproxy credentials.</li>
    <br/>
    <span class="errorMessage"><s:property value="#request['loginErrorMessage']"/></span>
    <s:form action="User_login">
      <s:textfield key="username" />
      <s:password key="password" showPassword="true" />
      <s:submit />
    </s:form>
  </ol>

  Note: This page uses myproxy2.arcs.org.au as the myproxy server.

</body>
</html>
