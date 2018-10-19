import _ from 'lodash'
import moment from 'moment/moment'

// if the input represents a finite number, coerces and returns it, else null
export const toJsonLd = item => {
  const parts = [
    basicToJsonLd(item),
    doiToJsonLd(item)
  ]

  return `{${_.join(_.compact(parts), ',')}
}`
//   return `{
//   "@context": "http://schema.org",
//   "@type": "Dataset",
//   "name": "${item.title}",
//   "description": "${item.description}"
// }`
}

export const basicToJsonLd = item => {
  return `
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "${item.title}",
  "description": "${item.description}"`
}

export const doiToJsonLd = item => {
  if (item.doi)
  return `
  "alternateName": "${item.doi}",
  "url": "https://accession.nodc.noaa.gov/${item.doi}",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=${item.doi}"`
}
