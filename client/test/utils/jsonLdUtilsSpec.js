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
  describe('an empty map for input', function () {
    const input = {}

    it('does not break the utility functions', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset"
        }`
      )
    })
  })

  describe('a collection with no optional fields', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      fileIdentifier: "gov.test.cires.example:abc",
    }

    it('creates a very simple JSON-LD object', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
          "identifier" : [
             {
                "value" : "gov.test.cires.example:abc",
                "propertyID" : "NCEI Dataset Identifier",
                "@type" : "PropertyValue"
             }
          ]
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

    it('does not generate a distribution block', function () {
      assert.equal(util.downloadLinksToDistributionJsonLd(input), null)
    })

  })

  describe('a collection with a doi', function () {
    const input = {
      title: "the title of the record",
      doi: "doi:10.1234/ABCDEFGH",
    }

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
      thumbnail: "http://example.com/thumbnail",
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
      beginDate: "2018-10-19",
      endDate: "2019-01-02",
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
          "temporalCoverage": "2018-10-19/2019-01-02"
        }`
      )
    })
  })

  describe('a collection with an unbounded date range', function () {
    const input = {
      title: "the title of the record",
      beginDate: "2018-10-19",
    }

    it('generates an temporal block', function () {
      jsonEquals(util.temporalToJsonLd(input), `"temporalCoverage": "2018-10-19/.."`)
    })
  })

  describe('a collection with a missing start date', function () {
    const input = {
      title: "the title of the record",
      endDate: "2018-10-19",
    }

    it('generates an temporal block', function () {
      jsonEquals(util.temporalToJsonLd(input), `"temporalCoverage": "../2018-10-19"`)
    })
  })

  describe('a collection with an instant date', function () {
    const input = {
      beginDate: "2018-10-19",
      endDate: "2018-10-19",
    }

    it('generates an temporal block', function () {
      jsonEquals(util.temporalToJsonLd(input), `"temporalCoverage": "2018-10-19"`)
    })
  })

  describe('a collection with a bounding box', function () {
    const input = {
      title: "the title of the record",
      spatialBounding: {
        coordinates: [
          [
           [-180,32],[-116,32],[-116,62],[-180,62],[-180,32]
          ]
        ],
        type: "Polygon"
      },
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
      spatialBounding: {
        coordinates: [
          [-7.7,51.5],
          [-7.7,51.6]
        ],
        type: "LineString"
      },
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
      spatialBounding: {
        coordinates: [
          [-49.815, 69.222]
        ],
        type: "Point"
      },
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

  it('generates keyword places', function () {
    const testCases = [
      {
        input: "Continent > North America > United States Of America",
        output: `{
          "@type": "Place",
          "name": "Continent > North America > United States Of America"
        }`
      },
      {
        input: "Ocean > Pacific Ocean > North Pacific Ocean",
        output: `{
          "@type": "Place",
          "name": "Ocean > Pacific Ocean > North Pacific Ocean"
        }`
      },
      {
        input: "Vertical Location > Land Surface",
        output: `{
          "@type": "Place",
          "name": "Vertical Location > Land Surface"
        }`
      },
      {
        input: "Vertical Location > Sea Floor",
        output: `{
          "@type": "Place",
          "name": "Vertical Location > Sea Floor"
        }`
      },
    ]

    testCases.forEach((c) => {
      jsonEquals(util.placenameToJsonLd(c.input), c.output)
    })
  })

  describe('a collection with gcmdLocations', function () {
    const input = {
      title: "the title of the record",
      gcmdLocations: [
        "Continent > North America > United States Of America",
        "Continent > North America",
        "Continent",
        "Ocean > Pacific Ocean > North Pacific Ocean",
        "Ocean > Pacific Ocean",
        "Ocean",
        "Vertical Location > Land Surface",
        "Vertical Location",
        "Vertical Location > Sea Floor",
      ],
      keywords: [
        "Oceans > Bathymetry/Seafloor Topography > Seafloor Topography",
        "Oceans > Bathymetry/Seafloor Topography > Bathymetry",
        "Oceans > Bathymetry/Seafloor Topography > Water Depth",
        "Land Surface > Topography > Terrain Elevation",
        "Land Surface > Topography > Terrain Elevation > Topographical Relief Maps",
        "Oceans > Coastal Processes > Coastal Elevation",
        "Models/Analyses > DEM > Digital Elevation Model",
        "ICSU-WDS > International Council for Science - World Data System",
        "< 1 meter",
        "Coastal Relief",
        "Gridded elevations",
        "Integrated bathymetry and topography",
        "Continent > North America > United States Of America",
        "Ocean > Pacific Ocean > North Pacific Ocean",
        "Vertical Location > Land Surface",
        "Vertical Location > Sea Floor",
        "DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce",
        "DOC/NOAA/NESDIS/NGDC > National Geophysical Data Center, NESDIS, NOAA, U.S. Department of Commerce"
      ],
    }

    it('identifies the correct place keywords', function () {
      assert.deepEqual(
        util.spatialKeywordsSubset(input),
        [
          "Continent > North America > United States Of America",
          "Ocean > Pacific Ocean > North Pacific Ocean",
          "Vertical Location > Land Surface",
          "Vertical Location > Sea Floor"
        ]
      )
    })

    it('generates a spatial block', function () {
      jsonEquals(
        util.spatialToJsonLd(input),
        `"spatialCoverage": [
          {
            "@type": "Place",
            "name": "Continent > North America > United States Of America"
          },
          {
            "@type": "Place",
            "name": "Ocean > Pacific Ocean > North Pacific Ocean"
          },
          {
            "@type": "Place",
            "name": "Vertical Location > Land Surface"
          },
          {
            "@type": "Place",
            "name": "Vertical Location > Sea Floor"
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
          "spatialCoverage": [
            {
              "@type": "Place",
              "name": "Continent > North America > United States Of America"
            },
            {
              "@type": "Place",
              "name": "Ocean > Pacific Ocean > North Pacific Ocean"
            },
            {
              "@type": "Place",
              "name": "Vertical Location > Land Surface"
            },
            {
              "@type": "Place",
              "name": "Vertical Location > Sea Floor"
            }
          ]
        }`
      )
    })

  })

  it('generates download links', function () {
    const testCases = [
      {
        input: {
          linkUrl: "http://example.com/download",
          linkDescription: "an example link",
          linkName: "get data here",
          linkProtocol: "http",
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "description": "an example link",
          "name": "get data here",
          "encodingFormat": "http"
        }`
      },
      {
        input: {
          linkUrl: "http://example.com/download",
          linkName: "get data here",
          linkProtocol: "http",
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "name": "get data here",
          "encodingFormat": "http"
        }`
      },
      {
        input: {
          linkUrl: "http://example.com/download",
          linkDescription: "an example link",
          linkProtocol: "http",
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "description": "an example link",
          "encodingFormat": "http"
        }`
      },
      {
        input: {
          linkUrl: "http://example.com/download",
          linkDescription: "an example link",
          linkName: "get data here",
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "description": "an example link",
          "name": "get data here"
        }`
      },
      {
        input: {
          linkUrl: "http://example.com/download",
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download"
        }`
      },
    ]

    testCases.forEach((c) => {
      jsonEquals(util.linkToJsonLd(c.input), c.output)
    })
  })

  describe('a collection with download links', function () {
    const input = {
      title: "the title of the record",
      links: [
        {
          linkUrl: "http://example.com/download_1",
          linkDescription: "an example link",
          linkName: "get data here",
          linkProtocol: "http",
          linkFunction: "download",
        },
        {
          linkUrl: "http://example.com/download_2",
          linkDescription: "another link",
          linkName: "cloud",
          linkProtocol: "s3",
          linkFunction: "download",
        },
      ],
    }

    it('generates a distribution block', function () {
      jsonEquals(
        util.downloadLinksToDistributionJsonLd(input),
        `"distribution": [
          {
            "@type": "DataDownload",
            "url": "http://example.com/download_1",
            "description": "an example link",
            "name": "get data here",
            "encodingFormat": "http"
          },
          {
            "@type": "DataDownload",
            "url": "http://example.com/download_2",
            "description": "another link",
            "name": "cloud",
            "encodingFormat": "s3"
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
          "distribution": [
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_1",
              "description": "an example link",
              "name": "get data here",
              "encodingFormat": "http"
            },
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_2",
              "description": "another link",
              "name": "cloud",
              "encodingFormat": "s3"
            }
          ]
        }`
      )
    })
  })

  describe('a collection with information links', function () {
    const input = {
      title: "the title of the record",
      links: [
        {
          linkUrl: "http://example.com/help",
          linkDescription: "helpful info",
          linkName: "info page",
          linkProtocol: "http",
          linkFunction: "information",
        },
      ],
    }

    it('does not generate a distribution block', function () {
      assert.equal(util.downloadLinksToDistributionJsonLd(input), null)
    })

    it('generates json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record"
        }`
      )
    })
  })

  describe('a collection with just a fileIdentifier', function () {
    const input = {
      title: "the title of the record",
      fileIdentifier: "gov.test.cires.example:abc",
    }

    it('generates an id from the fileIdentifier', function () {
      jsonEquals(
        util.fileIdentifierToJsonLd(input),
        `{
           "value" : "gov.test.cires.example:abc",
           "propertyID" : "NCEI Dataset Identifier",
           "@type" : "PropertyValue"
        }`
      )
    })

    it('generates a simple idendifier block', function () {
      jsonEquals(
        util.identifiersToJsonLd(input),
        `"identifier" : [
           {
              "value" : "gov.test.cires.example:abc",
              "propertyID" : "NCEI Dataset Identifier",
              "@type" : "PropertyValue"
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
          "identifier" : [
             {
                "value" : "gov.test.cires.example:abc",
                "propertyID" : "NCEI Dataset Identifier",
                "@type" : "PropertyValue"
             }
          ]
        }`
      )
    })
  })

  describe('a complete collection', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      fileIdentifier: "gov.test.cires.example:abc",
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
      gcmdLocations: [
        "Continent > North America > United States Of America",
        "Vertical Location > Sea Floor",
      ],
      keywords: [
        "Continent > North America > United States Of America",
        "Vertical Location > Sea Floor",
      ],
      links: [
        {
          linkUrl: "http://example.com/download_1",
          linkDescription: "an example link",
          linkName: "get data here",
          linkProtocol: "http",
          linkFunction: "download",
        },
        {
          linkUrl: "http://example.com/download_2",
          linkDescription: "another link",
          linkName: "cloud",
          linkProtocol: "s3",
          linkFunction: "download",
        },
        {
          linkUrl: "http://example.com/help",
          linkDescription: "helpful info",
          linkName: "info page",
          linkProtocol: "http",
          linkFunction: "information",
        },
      ]
    }

    it('generates full json-ld', function () {
      jsonEquals(
        util.toJsonLd(input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "description": "A rather long description (not!)",
          "identifier" : [
             {
                "value" : "gov.test.cires.example:abc",
                "propertyID" : "NCEI Dataset Identifier",
                "@type" : "PropertyValue"
             }
          ],
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
            },
            {
              "@type": "Place",
              "name": "Continent > North America > United States Of America"
            },
            {
              "@type": "Place",
              "name": "Vertical Location > Sea Floor"
            }
          ],
          "distribution": [
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_1",
              "description": "an example link",
              "name": "get data here",
              "encodingFormat": "http"
            },
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_2",
              "description": "another link",
              "name": "cloud",
              "encodingFormat": "s3"
            }
          ]
        }`
      )
    })
  })


})
