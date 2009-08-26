<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html PUBLIC
  "-//W3C//DTD XHTML 1.1 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>DTS Job Status Details Page</title>
  <s:head />
</head>
<body>
  <h3>Job Status Details</h3>
  Job id: <s:property value="#request.jobResourceKey"/><br/>
  Status: <s:property value="#request.jobStatus"/>
  <br/>
  <br/>
  <a href="DtsMenu_display.action">back</a>
</body>
</html>
