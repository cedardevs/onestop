package org.cedar.onestop.api.search.controller

import java.time.Instant

class SitemapGenerator {


  static String makeSitemap(String baseUrl, def sitemapData) {
    def data = sitemapData.collect({site -> """
    <sitemap>
      <loc>${baseUrl}/sitemap/${site.id}.txt</loc>
      <lastmod>${Instant.ofEpochMilli(site.attributes.lastUpdatedDate).toString()}</lastmod>
    </sitemap>
    """}).join('\n')

      return """
    <?xml version="1.0" encoding="UTF-8"?>
      <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      ${data}
    </sitemapindex>
      """
  }

  static String makeSiteSubmap(String baseUrl, def submapData) {
    return submapData[0].attributes.content.collect({collectionId -> "${baseUrl}/collections/details/${collectionId}"}).join('\n')
  }

}
