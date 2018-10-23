import '../specHelper'
// import { toJsonLd, doiToJsonLd } from '../../src/utils/jsonLdUtils'
import * as util from '../../src/utils/jsonLdUtils'
import {assert} from 'chai'

const jsonEquals = (expected, actual) => {
  // compare multiline strings after removing leading whitespace, making it easier to have sensible looking tests
  assert.equal(expected.replace(/^\s\s*/gm, ""), actual.replace(/^\s\s*/gm, ""))
}

describe('In the jsonLdUtils', function () {

  // Note: resulting JsonLD verified using https://search.google.com/structured-data/testing-tool/u/0/

  describe('a collection with no optional fields', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)"
    }

    it('creates a very simple JSON-LD object', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)"
        }`
      )
    })

    it('does not generate a doi block', function () {
      assert.equal(util.doiToJsonLd(input), null)
    })

    it('does not generate a thumbnail image block', function () {
      assert.equal(util.thumbnailToJsonLd(input), null)
    })

    it('does not generate a temporal block', function () {
      assert.equal(util.temporalToJsonLd(input), null)
    })

    it('does not generate a spatial block', function () {
      assert.equal(util.spatialToJsonLd(input), null)
    })

  })

  describe('a collection with a doi', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      doi: "doi:10.1234/ABCDEFGH"
    }

  // TODO   const expectedBlock = `
  // "alternateName": "doi:10.1234/ABCDEFGH",
  // "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  // "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"`

    it('generates a doi block', function () {
      jsonEquals(
        util.doiToJsonLd(input),
        `"alternateName": "doi:10.1234/ABCDEFGH",
        "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
        "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"`
      )
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
          "alternateName": "doi:10.1234/ABCDEFGH",
          "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
          "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"
        }`
      )
    })
  })

  describe('a collection with a thumbnail', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      thumbnail: "http://example.com/thumbnail"
    }

    it('generates an image block', function () {
      jsonEquals(
        util.thumbnailToJsonLd(input),
        `"image": {
          "@type": "ImageObject",
          "url" : "http://example.com/thumbnail",
          "contentUrl" : "http://example.com/thumbnail"
        }`
      )
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
          "image": {
            "@type": "ImageObject",
            "url" : "http://example.com/thumbnail",
            "contentUrl" : "http://example.com/thumbnail"
          }
        }`
      )
    })
  })

  describe('a collection with a bounded date range', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      beginDate: "2018-10-19",
      endDate: "2019-01-02"
    }

    it('generates an temporal block', function () {
      jsonEquals(util.temporalToJsonLd(input), `"temporalCoverage": "2018-10-19/2019-01-02"`)
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
          "temporalCoverage": "2018-10-19/2019-01-02"
        }`
      )
    })
  })

  describe('a collection with an unbounded date range', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      beginDate: "2018-10-19",
    }

    it('generates an temporal block', function () {
      jsonEquals(util.temporalToJsonLd(input), `"temporalCoverage": "2018-10-19/undefined"`)
    })
  })

  describe('a collection with a bounding box', function () {
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

    it('generates a geo shape', function () {
      jsonEquals(
        util.buildCoordinatesString(input),
        `{
          "@type": "Place",
          "name": "geographic bounding box",
          "geo": {
            "@type": "GeoShape",
            "description": "minY,minX maxY,maxX",
            "box": "32,-180 62,-116"
          }
        }`
      )
    })

    it('generates a spatial block', function () {
      jsonEquals(
        util.spatialToJsonLd(input),
        `"spatialCoverage": [
          {
            "@type": "Place",
            "name": "geographic bounding box",
            "geo": {
              "@type": "GeoShape",
              "description": "minY,minX maxY,maxX",
              "box": "32,-180 62,-116"
            }
          }
        ]`
      )
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
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
        }`
      )
    })
  })

  describe('a collection with a line', function () {
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

    it('generates a geo shape', function () {
      jsonEquals(
        util.buildCoordinatesString(input),
        `{
          "@type": "Place",
          "name": "geographic bounding line",
          "geo": {
            "@type": "GeoShape",
            "description": "y,x y,x",
            "line": "51.5,-7.7 51.6,-7.7"
          }
        }`
      )
    })

    it('generates a spatial block', function () {
      jsonEquals(
        util.spatialToJsonLd(input), `"spatialCoverage": [
          {
            "@type": "Place",
            "name": "geographic bounding line",
            "geo": {
              "@type": "GeoShape",
              "description": "y,x y,x",
              "line": "51.5,-7.7 51.6,-7.7"
            }
          }
        ]`
      )
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
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
          ]
        }`
      )
    })
  })

  describe('a collection with a line', function () {
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

    it('generates a geo shape', function () {
      jsonEquals(
        util.buildCoordinatesString(input),
        `{
          "@type": "Place",
          "name": "geographic bounding point",
          "geo": {
            "@type": "GeoCoordinates",
            "latitude": "69.222",
            "longitude": "-49.815"
          }
        }`
      )
    })

    it('generates a spatial block', function () {
      jsonEquals(
        util.spatialToJsonLd(input),
        `"spatialCoverage": [
          {
            "@type": "Place",
            "name": "geographic bounding point",
            "geo": {
              "@type": "GeoCoordinates",
              "latitude": "69.222",
              "longitude": "-49.815"
            }
          }
        ]`
      )
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
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
          ]
        }`
      )
    })
  })
//
//   //"gcmdLocations":["Continent > North America > United States Of America","Continent > North America","Continent","Ocean > Pacific Ocean > North Pacific Ocean","Ocean > Pacific Ocean","Ocean","Vertical Location > Land Surface","Vertical Location","Vertical Location > Sea Floor"]
//
//   it('adds spatial if gcmdLocations', function () {
//     const input = {
//       title: "the title of the record",
//       description: "A rather long description (not!)",
//       gcmdLocations: [
//         "Continent > North America > United States Of America",
//         "Continent > North America",
//         "Continent",
//         "Ocean > Pacific Ocean > North Pacific Ocean",
//         "Ocean > Pacific Ocean",
//         "Ocean",
//         "Vertical Location > Land Surface",
//         "Vertical Location",
//         "Vertical Location > Sea Floor"],
//         keywords: ["Oceans > Bathymetry/Seafloor Topography > Seafloor Topography", "Oceans > Bathymetry/Seafloor Topography > Bathymetry", "Oceans > Bathymetry/Seafloor Topography > Water Depth", "Land Surface > Topography > Terrain Elevation", "Land Surface > Topography > Terrain Elevation > Topographical Relief Maps", "Oceans > Coastal Processes > Coastal Elevation", "Models/Analyses > DEM > Digital Elevation Model", "ICSU-WDS > International Council for Science - World Data System", "< 1 meter", "Coastal Relief", "Gridded elevations", "Integrated bathymetry and topography", "Continent > North America > United States Of America", "Ocean > Pacific Ocean > North Pacific Ocean", "Vertical Location > Land Surface", "Vertical Location > Sea Floor", "DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce", "DOC/NOAA/NESDIS/NGDC > National Geophysical Data Center, NESDIS, NOAA, U.S. Department of Commerce"],
//
//     }
//
//     jsonEquals(util.spatialKeywordsToJsonLd(input), `
//   "spatialCoverage": [
//     {
//       "@type": "Place",
//       "name": "Continent > North America > United States Of America"
//     },
//     {
//       "@type": "Place",
//       "name": "Ocean > Pacific Ocean > North Pacific Ocean"
//     },
//     {
//       "@type": "Place",
//       "name": "Vertical Location > Land Surface"
//     },
//     {
//       "@type": "Place",
//       "name": "Vertical Location > Sea Floor"
//     }
//   ]`)
//
//     jsonEquals(util.spatialKeywordsToJsonLd({}), null, 'no coordinates in map should return null for spatialToJsonLd helper')
//
//     jsonEquals(util.toJsonLd(input), `{
//   "@context": "http://schema.org",
//   "@type": "Dataset",
//   "name": "the title of the record",
//   "description": "A rather long description (not!)",
//   "spatialCoverage": [
//     {
//       "@type": "Place",
//       "name": "Continent > North America > United States Of America"
//     },
//     {
//       "@type": "Place",
//       "name": "Ocean > Pacific Ocean > North Pacific Ocean"
//     },
//     {
//       "@type": "Place",
//       "name": "Vertical Location > Land Surface"
//     },
//     {
//       "@type": "Place",
//       "name": "Vertical Location > Sea Floor"
//     }
//   ]
// }`)
//   })
//
  describe('a complete collection', function () {
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

    it('generates full json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
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
        }`
      )
    })
  })


})
