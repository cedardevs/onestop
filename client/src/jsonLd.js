const rootUrl = `${window.location.origin + window.location.pathname}`

const jsonLdScript = document.createElement('script')
jsonLdScript.setAttribute('type', 'application/ld+json')
jsonLdScript.insertAdjacentHTML(
  'afterbegin',
  `{
  "@context": "http://schema.org",
  "@type": "WebSite",
  "@id": "${rootUrl}",
  "url": "${rootUrl}",
  "potentialAction": {
    "@type": "SearchAction",
    "target": "${rootUrl}#/collections?q={search_term_string}",
    "query-input": "required name=search_term_string"
  },
  "publisher": {
    "@type": "Organization",
    "@id": "https://www.ncei.noaa.gov/",
    "name": "National Centers for Environmental Information (NCEI)",
    "logo": {
        "@type": "ImageObject",
        "url": "https://www.ncei.noaa.gov/sites/default/files/noaa_logo_circle_72x72.svg",
        "width": "72",
        "height": "72"
    }
  }
}`
)
document.body.appendChild(jsonLdScript)
