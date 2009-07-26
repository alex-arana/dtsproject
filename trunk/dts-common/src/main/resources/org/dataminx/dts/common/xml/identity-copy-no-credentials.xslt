<?xml version="1.0" encoding="UTF-8"?>
<!--
  Intersect Pty Ltd (c) 2009
  License: To Be Announced

  Description:
    Stylesheet used to strip authentication credentials from a DTS XML message so that it can be used for
    logging or display purposes.  The resulting XML is not necessarily valid and it should not be used
    for processing purposes.

  Author:
    Alex Arana
 -->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dmi="http://schemas.ogf.org/dmi/2008/05/dmi"
    xmlns:dts="http://schemas.dataminx.org/dts/2009/07/messages"
    xmlns:jsdl="http://schemas.dataminx.org/dts/2009/07/jsdl"
    xmlns:jsdl1="http://schemas.ggf.org/jsdl/2005/11/jsdl"
    xmlns:oas="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="jsdl:Credential">
    <xsl:element name="Credential" namespace="http://schemas.dataminx.org/dts/2009/07/jsdl">
      <xsl:comment>NOTE: authentication credentials have been removed for security reasons</xsl:comment>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
