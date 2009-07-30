<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html PUBLIC
  "-//W3C//DTD XHTML 1.1 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>DTS Job Form Page</title>
  <s:head />
</head>
<body>
  <h3>DTS Job Form</h3>
  Please fill in the job form:
  <s:form action="DtsJob_submit">
      <s:textfield name="jobName" label="Job Name"/>

      <s:textfield name="srcUri" label="Source URI"/>
      <s:textfield name="srcCredUsername" label="Source Credential Username"/>
      <s:textfield name="srcCredPassword" label="Source Credential Password"/>

      <s:textfield name="targetUri" label="Target URI"/>
      <s:textfield name="targetCredUsername" label="Target Credential Username"/>
      <s:textfield name="targetCredPassword" label="Target Credential Password"/>
      <s:submit />
    </s:form>
    <a href="DtsJob_back.action">back</a>
</body>
</html>
