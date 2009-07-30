<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE html PUBLIC
  "-//W3C//DTD XHTML 1.1 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>DTS Menu Page</title>
  <s:head />
</head>
<body>
  <h3>Data Transfer Service Menu</h3>
  Hi <s:property value="#session['commonName']"/>,
  <p/>Please select a step you would like to perform:
  <ul>
    <li><a href="DtsJob_input.action">Submit a new job</a></li>
    <li>Query a job</li>
  </ul>
  <a href="User_logout.action">logout</a>
</body>
</html>
