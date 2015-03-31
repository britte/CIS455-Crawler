<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
  <h2>RSS Feed</h2>
  <ul style="list-style-type:none">
    <xsl:apply-templates/>
  </ul>
  </body>
  </html>
</xsl:template>

<xsl:template match="documentcollection/document">
	<li>
		<a>
			<xsl:attribute name="href"><xsl:value-of select="@location"/></xsl:attribute>
			<h3>
				<xsl:value-of select="rss/channel/title"/>
			</h3>
		</a>
		<ul>
			<xsl:apply-templates select="rss/channel/item"></xsl:apply-templates>
		</ul>
	</li>
	<hr/>
</xsl:template>

<xsl:template match="rss/channel/item">
	<xsl:if test="(title[contains(.,'war')]) or (title[contains(.,'peace')]) or 
				(description[contains(.,'war')]) or (description[contains(.,'peace')])">
	<li>
		<a>
			<xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
			<xsl:if test="title and string-length(title[text()]) > 0">
				<xsl:value-of select="title"/>
			</xsl:if>
			<xsl:if test="not(title and string-length(title[text()]) > 0)">
				<em>{No title given}</em>
			</xsl:if>
		</a>
		<xsl:if test="description and string-length(description[text()]) > 0">
			<ul><li><xsl:value-of select="description"/></li></ul>
		</xsl:if>
	</li>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>