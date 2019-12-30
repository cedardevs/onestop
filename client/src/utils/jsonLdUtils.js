import _ from 'lodash'
import moment from 'moment/moment'
import {isPolygonABoundingBox} from './geoUtils'

export const toJsonLd = (uuid, item, pageUrl) => {
  const parts = [
    `"@context": "http://schema.org",
    "@type": "Dataset"`,
    nameField(item),
    alternateNameField(item),
    descriptionField(item),
    identifierField(uuid, item),
    urlField(pageUrl),
    sameAsField(item),
    imageField(item),
    temporalCoverageField(item),
    spatialCoverageField(item),
    distributionField(item),
    keywordsField(item),
    encodingFormatField(item),
  ]

  // remove nulls and join
  return `{
    ${_.join(_.compact(parts), ',\n')}
  }`
}

export const nameField = item => {
  if (item.title) return `"name": "${item.title}"`
}

export const alternateNameField = item => {
  if (item.fileIdentifier) return `"alternateName": "${item.fileIdentifier}"`
}

export const descriptionField = item => {
  if (item.description) return `"description": "${item.description}"`
}

export const fileIdentifierListItem = item => {
  if (item.fileIdentifier)
    return `{
    "value": "${item.fileIdentifier}",
    "propertyID": "NCEI Dataset Identifier",
    "@type": "PropertyValue"
  }`
}

export const uuidListItem = uuid => {
  if (uuid)
    return `{
    "value": "${uuid}",
    "propertyID": "OneStop UUID",
    "@type": "PropertyValue"
  }`
}

export const identifierField = (uuid, item) => {
  const parts = [
    uuidListItem(uuid),
    fileIdentifierListItem(item),
    doiListItem(item),
  ]

  if (_.compact(parts).length > 0)
    // remove nulls and join
    return `"identifier": [
    ${_.join(_.compact(parts), ',\n')}
  ]`
}

export const doiListItem = item => {
  if (item.doi)
    return `{
    "value": "${item.doi}",
    "propertyID": "Digital Object Identifier (DOI)",
    "@type": "PropertyValue"
  }`
}

export const urlField = pageUrl => {
  if (pageUrl) return `"url": "${pageUrl}"`
}

export const sameAsField = item => {
  const parts = [
    item.doi ? `"https://doi.org/${item.doi}"` : null,
    item.fileIdentifier
      ? `"https://data.nodc.noaa.gov/cgi-bin/iso?id=${item.fileIdentifier}"`
      : null,
  ]

  if (_.compact(parts).length > 0)
    // remove nulls and join
    return `"sameAs": [
    ${_.join(_.compact(parts), ',\n')}
  ]`
}

export const imageField = item => {
  if (item.thumbnail)
    return `"image": {
    "@type": "ImageObject",
    "url": "${item.thumbnail}",
    "contentUrl": "${item.thumbnail}",
    "caption": "Preview graphic"
  }`
}

export const temporalCoverageField = item => {
  if (item.beginDate && item.endDate) {
    if (item.beginDate == item.endDate) {
      return `"temporalCoverage": "${item.beginDate}"`
    }
    else {
      return `"temporalCoverage": "${item.beginDate}/${item.endDate}"`
    }
  }
  if (item.beginDate) return `"temporalCoverage": "${item.beginDate}/.."`
  if (item.endDate) return `"temporalCoverage": "../${item.endDate}"`
}

export const spatialCoverageField = item => {
  const parts = _.concat([ geoListItem(item) ], placenameList(item))

  if (_.compact(parts).length > 0)
    // remove nulls and join
    return `"spatialCoverage": [
    ${_.join(_.compact(parts), ',\n')}
  ]`
}

export const scienceKeywordsSubset = item => {
  return _.intersection(item.keywords, item.gcmdScience)
}

export const locationKeywordsSubset = item => {
  return _.intersection(item.keywords, item.gcmdLocations)
}

export const placenameListItem = location => {
  return `{
    "@type": "Place",
    "name": "${location}"
  }`
}

export const placenameList = item => {
  // gcmdLocations has extra entries for each layer in the keywords, but the intersection with the original keywords correctly identifies the correct subset
  return _.map(locationKeywordsSubset(item), placenameListItem)
}

const polygonLd = coordinates => {
  if (isPolygonABoundingBox(coordinates)) {
    return `{
      "@type": "Place",
      "name": "geographic bounding box",
      "geo": {
        "@type": "GeoShape",
        "description": "minY,minX maxY,maxX",
        "box": "${coordinates[0][0][1]},${coordinates[0][0][0]} ${coordinates[0][2][1]},${coordinates[0][2][0]}"
      }
    }`
  }
  return `{
  "@type": "Place",
  "name": "geographic polygon",
  "geo": {
    "@type": "GeoShape",
    "description": "y,x y,x ...",
    "polygon": "${coordinates[0]
      .map(it => {
        return `${it[1]},${it[0]}`
      })
      .join(' ')}"
  }
}`
}

export const geoListItem = item => {
  const geometry = item.spatialBounding
  // For point, want GeoCoordnates: longitude:[0], latitude:[1]
  // The geographic shape of a place. A GeoShape can be described using several properties whose values are based on latitude/longitude pairs. Either whitespace or commas can be used to separate latitude and longitude; whitespace should be used when writing a list of several such points.
  // For line, want GeoShape: line: y,x y,x
  // A line is a point-to-point path consisting of two or more points. A line is expressed as a series of two or more point objects separated by space.
  // For polygon want GeoShape:  box: minY,minX maxY,maxX ([0] [2])
  // A box is the area enclosed by the rectangle formed by two points. The first point is the lower corner, the second point is the upper corner. A box is expressed as two points separated by a space character.
  if (geometry) {
    if (geometry.type.toLowerCase() === 'point') {
      return `{
      "@type": "Place",
      "name": "geographic bounding point",
      "geo": {
        "@type": "GeoCoordinates",
        "latitude": "${geometry.coordinates[0][1]}",
        "longitude": "${geometry.coordinates[0][0]}"
      }
    }`
    }
    else if (geometry.type.toLowerCase() === 'linestring') {
      return `{
      "@type": "Place",
      "name": "geographic bounding line",
      "geo": {
        "@type": "GeoShape",
        "description": "y,x y,x",
        "line": "${geometry.coordinates[0][1]},${geometry
        .coordinates[0][0]} ${geometry.coordinates[1][1]},${geometry
        .coordinates[1][0]}"
      }
    }`
    }
    else if (geometry.type.toLowerCase() === 'polygon') {
      return polygonLd(geometry.coordinates)
    }
    else if (geometry.type.toLowerCase() === 'multipolygon') {
      return `${geometry.coordinates
        .map(it => {
          return polygonLd(it)
        })
        .join(', ')}`
    }
  }
  else {
    // return 'No spatial bounding provided.'
  }
}

export const distributionField = item => {
  if (!item.links) return undefined
  return `"distribution": [
    ${_.join(_.map(item.links, downloadLinkList), ',\n')}
  ]`
}

export const downloadLinkList = link => {
  const {linkUrl, linkName, linkProtocol, linkDescription, linkFunction} = link

  const disambiguation = `${linkFunction || 'download'} (${linkProtocol ||
    'HTTP'})`
  const parts = [
    `"@type": "DataDownload"`,
    linkUrl ? `"url": "${linkUrl}"` : null,
    linkDescription ? `"description": "${linkDescription}"` : null,
    `"disambiguatingDescription": "${disambiguation}"`,
    linkName ? `"name": "${linkName}"` : null,
    linkProtocol ? `"encodingFormat": "${linkProtocol}"` : null,
  ]

  // remove nulls and join
  return `{
    ${_.join(_.compact(parts), ',\n')}
  }`
}

export const keywordsField = item => {
  // remove empty strings
  const parts = _.remove(scienceKeywordsSubset(item), function(word){
    return word != ''
  })

  if (_.compact(parts).length > 0)
    // remove nulls and join
    return `"keywords": [
    ${_.join(_.map(_.compact(parts), keyword => `"${keyword}"`), ',\n')}
  ]`
}

export const encodingFormatField = item => {
  if (item.dataFormats)
    return `"encodingFormat": [
    ${_.join(
      _.map(item.dataFormats, format => {
        if (format.name && format.version)
          return `"${format.name} ${format.version}"`
        return `"${format.name}"`
      }),
      ',\n'
    )}
  ]`
}

export const appJsonLd = rootUrl => {
  return `{
    "@context": "http://schema.org",
    "@type": "WebSite",
    "@id": "${rootUrl}",
    "url": "${rootUrl}",
    "potentialAction": {
      "@type": "SearchAction",
      "target": "${rootUrl}collections?q={search_term_string}",
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
}
