<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY atsign "&#64;">
  <!ENTITY doubleslash "&#47;&#47;">
]>
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

  <!-- strip all comments from the input -->
  <xsl:template match="comment()"/>

  <xsl:template match="jsdl:Credential">
    <xsl:element name="Credential" namespace="http://schemas.dataminx.org/dts/2009/07/jsdl">
      <xsl:comment>NOTE: authentication credentials have been removed for security reasons</xsl:comment>
    </xsl:element>
  </xsl:template>

  <!-- handle embedded URI credentials scenario.  eg. username:password@URI -->
  <xsl:template match="jsdl1:URI">
    <xsl:element name="URI" namespace="http://schemas.ggf.org/jsdl/2005/11/jsdl">
      <!--
        This next line should work with an XSLT 2.0 compliant engine.  The regular expression used in it
        is a variation of that suggested by Tim Berners Lee on Appendix B of RFC-3986:
        http://www.apps.ietf.org/rfc/rfc3986.html (Parsing a URI Reference with a Regular Expression)
      -->
      <!--<xsl:value-of select="fn:replace(., '^(([^:/?#]+):)?(//+)?(([^/?#]+)@)?(.*)?', '$1$3$6')"/>-->
      <xsl:call-template name="stripCredentials">
        <xsl:with-param name="text" select="."/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>

  <!--
    Strips any embedded authentication credentials from the input URI.
    It follows the specification for Uniform Resource Locators (URL): Generic Syntax
    http://www.apps.ietf.org/rfc/rfc3986.html (T. Berners Lee et al)
   -->
  <xsl:template name="stripCredentials">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text, '&atsign;')">
        <xsl:variable name="trailing" select="substring-after($text, '&atsign;')"/>
        <xsl:choose>
          <xsl:when test="contains($text, '&doubleslash;')">
            <xsl:value-of select="concat(substring-before($text, '&doubleslash;'), '&doubleslash;', $trailing)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$trailing"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
