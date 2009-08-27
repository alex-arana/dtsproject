<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html PUBLIC
  "-//W3C//DTD XHTML 1.1 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>DTS Get Job Status Form Page</title>
  <s:head />
</head>
<body>
  <h3>DTS Get Job Status Form</h3>

  <span class="errorMessage"><s:property value="#request['submitJobErrorMessage']"/></span>

  <p/>Please provide the id of the job you are inquiring about:
  <s:form action="DtsJobStatus_getDetails">
      <s:textfield name="jobResourceKey" label="Job Id"/>
      <s:submit />
  </s:form>
  <a href="DtsMenu_display.action">back</a>
</body>
</html>
