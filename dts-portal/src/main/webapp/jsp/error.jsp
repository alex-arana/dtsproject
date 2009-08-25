<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html PUBLIC
  "-//W3C//DTD XHTML 1.1 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>DTS Error Page</title>
  <s:head />
</head>
<body>
  <h3>Exception thrown by the service</h3>

    <s:property value="exception.message"/>
    <s:property value="exceptionStack"/>

    <a href="DtsJob_back.action">back</a>
</body>
</html>
