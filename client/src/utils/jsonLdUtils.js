import _ from 'lodash'
import moment from 'moment/moment'

// if the input represents a finite number, coerces and returns it, else null
export const toJsonLd = item => {
  const parts = [
    basicToJsonLd(item),
    doiToJsonLd(item),
    thumbnailToJsonLd(item),
    temporalToJsonLd(item),
    spatialToJsonLd(item),
  ]

  // remove nulls and join
  return `{
    ${_.join(_.compact(parts), ',\n')}
  }`
}

export const basicToJsonLd = item => {
  return `"@context": "http://schema.org",
  "@type": "Dataset",
  "name": "${item.title}",
  "description": "${item.description}"`
}

export const doiToJsonLd = item => {
  if (item.doi)
  return `"alternateName": "${item.doi}",
  "url": "https://accession.nodc.noaa.gov/${item.doi}",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=${item.doi}"`
}

export const thumbnailToJsonLd = item => {
  if (item.thumbnail)
  return `"image": {
    "@type": "ImageObject",
    "url" : "${item.thumbnail}",
    "contentUrl" : "${item.thumbnail}"
  }`
}

export const temporalToJsonLd = item => {
  if (item.beginDate)
  return `"temporalCoverage": "${item.beginDate}/${item.endDate}"`
}

export const spatialToJsonLd = item => {
  const parts = _.concat([],
    [buildCoordinatesString(item)],
    spatialKeywordsToJsonLd(item)
  )

  if( _.compact(parts).length > 0)
     // remove nulls and join
  return `"spatialCoverage": [
    ${_.join(_.compact(parts), ',\n')}
  ]`
  // if (item.spatialBounding)
  // return buildCoordinatesString(item.spatialBounding)
}

export const spatialKeywordsToJsonLd = item => {
  return _.map(_.intersection(item.keywords, item.gcmdLocations), location => {
    return `{
      "@type": "Place",
      "name": "${location}"
    }`
  })
}

export const buildCoordinatesString = item => {
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
        "line": "${geometry.coordinates[0][1]},${geometry.coordinates[0][0]} ${geometry.coordinates[1][1]},${geometry.coordinates[1][0]}"
      }
    }`
    }
    else {
      return `{
      "@type": "Place",
      "name": "geographic bounding box",
      "geo": {
        "@type": "GeoShape",
        "description": "minY,minX maxY,maxX",
        "box": "${geometry.coordinates[0][0][1]},${geometry.coordinates[0][0][0]} ${geometry.coordinates[0][2][1]},${geometry.coordinates[0][2][0]}"
      }
    }`
    }
  }
  else {
    // return 'No spatial bounding provided.'
  }
}
