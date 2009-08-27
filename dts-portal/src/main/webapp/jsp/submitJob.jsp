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

  <span class="errorMessage"><s:property value="#request.submitJobErrorMessage"/></span>
  <s:if test="#request.jobResourceKey != null">
      Job has been submitted. Please take note the of the job ID below because we are currently not keeping track
      of all the jobs that you submit to the DTS WebService. If you want to know the status of the job, you can either
      go to the link provided below or go DTS Menu's <a href="DtsJobStatus_input.action">Get Job Status page</a>.
      <p/>Job Id: <a href="DtsJobStatus_getDetails.action?jobResourceKey=<s:property value="#request.jobResourceKey"/>"><s:property value="#request.jobResourceKey"/></a>

      <p/>Submit another job?
  </s:if>

  <p/>Please fill in the job form:
  <s:form action="DtsJob_submit">
      <s:textfield name="name" label="Job Name" value="testjob"/>

      <s:textfield name="sourceUri" label="Source URI" value="ftp://mirror.aarnet.edu.au/pub/firefox/3.5rc3/linux-i686/ta/firefox-3.5rc3.tar.bz2" size="50"/>
      <s:textfield name="sourceCredUsername" label="Source Credential Username"/>
      <s:password name="sourceCredPassword" label="Source Credential Password"/>

      <s:textfield name="targetUri" label="Target URI" value="gsiftp://ng2.sapac.edu.au/tmp/ff-3.5rc3.tar.bz2" size="50"/>
      <s:textfield name="targetCredUsername" label="Target Credential Username"/>
      <s:password name="targetCredPassword" label="Target Credential Password"/>
      <s:submit />
    </s:form>
    <a href="DtsMenu_display.action">back</a>
</body>
</html>
