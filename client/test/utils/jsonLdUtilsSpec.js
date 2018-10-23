import '../specHelper'
// import { toJsonLd, doiToJsonLd } from '../../src/utils/jsonLdUtils'
import * as util from '../../src/utils/jsonLdUtils'
import {assert} from 'chai'

describe('The jsonLdUtils', function() {

  // Note: resulting JsonLD verified using https://search.google.com/structured-data/testing-tool/u/0/

  it('creates a very simple JSON-LD object', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)"
    }

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)"
}`)
  })

  it('adds some DOI specific fields if the doi is provided', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      doi: "doi:10.1234/ABCDEFGH"
    }

    assert.equal(util.doiToJsonLd(input), `
  "alternateName": "doi:10.1234/ABCDEFGH",
  "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"`)

    assert.equal(util.doiToJsonLd({}), null, 'no doi in map should return null for doiToJsonLd helper')

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "alternateName": "doi:10.1234/ABCDEFGH",
  "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"
}`)
  })

  it('adds image if thumbnail is provided', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      thumbnail: "http://example.com/thumbnail"
    }

    assert.equal(util.thumbnailToJsonLd(input), `
  "image": {
    "@type": "ImageObject",
    "url" : "http://example.com/thumbnail",
    "contentUrl" : "http://example.com/thumbnail"
  }`)

    assert.equal(util.thumbnailToJsonLd({}), null, 'no thumbnail in map should return null for thumbnailToJsonLd helper')

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "image": {
    "@type": "ImageObject",
    "url" : "http://example.com/thumbnail",
    "contentUrl" : "http://example.com/thumbnail"
  }
}`)
  })

  it('adds temporal if begin and end date provided', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      beginDate: "2018-10-19",
      endDate: "2019-01-02"
    }

    assert.equal(util.temporalToJsonLd(input), `
  "temporalCoverage": "2018-10-19/2019-01-02"`)

    assert.equal(util.temporalToJsonLd({}), null, 'no date in map should return null for temporalToJsonLd helper')

    assert.equal(util.temporalToJsonLd({beginDate: "2018-10-19"}), `
  "temporalCoverage": "2018-10-19/undefined"`, 'unbounded date range') // TODO this passed in the structured data parser, but is it really ok?

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "temporalCoverage": "2018-10-19/2019-01-02"
}`)
  })

  it('adds spatial if polygon', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      spatialBounding: {
        coordinates: [
          [
           [-180,32],[-116,32],[-116,62],[-180,62],[-180,32]
          ]
        ],
        type: "Polygon"
      }
    }

    assert.equal(util.spatialToJsonLd(input), `
  "spatialCoverage": [
    {
      "@type": "Place",
      "name": "geographic bounding box",
      "geo": {
        "@type": "GeoShape",
        "description": "minY,minX maxY,maxX",
        "box": "32,-180 62,-116"
      }
    }
  ]`)

    assert.equal(util.spatialToJsonLd({}), null, 'no coordinates in map should return null for spatialToJsonLd helper')

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "spatialCoverage": [
    {
      "@type": "Place",
      "name": "geographic bounding box",
      "geo": {
        "@type": "GeoShape",
        "description": "minY,minX maxY,maxX",
        "box": "32,-180 62,-116"
      }
    }
  ]
}`)
  })

  it('adds spatial if point', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      spatialBounding: {
        coordinates: [
          [-49.815, 69.222]
        ],
        type: "Point"
      }
    }

    assert.equal(util.spatialToJsonLd(input), `
  "spatialCoverage": [
    {
      "@type": "Place",
      "name": "geographic bounding point",
      "geo": {
        "@type": "GeoCoordinates",
        "latitude": "69.222",
        "longitude": "-49.815"
      }
    }
  ]`)

  })

  it('adds spatial if line', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      spatialBounding: {
        coordinates: [
          [-7.7,51.5],
          [-7.7,51.6]
        ],
        type: "LineString"
      }
    }

    assert.equal(util.spatialToJsonLd(input), `
  "spatialCoverage": [
    {
      "@type": "Place",
      "name": "geographic bounding line",
      "geo": {
        "@type": "GeoShape",
        "description": "y,x y,x",
        "line": "51.5,-7.7 51.6,-7.7"
      }
    }
  ]`)

  })

  it('full item', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      doi: "doi:10.1234/ABCDEFGH",
      thumbnail: "http://example.com/thumbnail",
      beginDate: "2018-10-19",
      endDate: "2019-01-02",
      spatialBounding: {
        coordinates: [
          [
            [-180, -90],
            [180, -90],
            [180, 90],
            [-180, 90],
            [-180, -90]
          ]
        ],
        type: "Polygon"
      },
    }

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "alternateName": "doi:10.1234/ABCDEFGH",
  "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH",
  "image": {
    "@type": "ImageObject",
    "url" : "http://example.com/thumbnail",
    "contentUrl" : "http://example.com/thumbnail"
  },
  "temporalCoverage": "2018-10-19/2019-01-02",
  "spatialCoverage": [
    {
      "@type": "Place",
      "name": "geographic bounding box",
      "geo": {
        "@type": "GeoShape",
        "description": "minY,minX maxY,maxX",
        "box": "-90,-180 90,180"
      }
    }
  ]
}`)
  })
})
