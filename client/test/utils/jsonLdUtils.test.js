import * as util from '../../src/utils/jsonLdUtils'

const jsonEquals = (expected, actual) => {
  // compare multiline strings after removing leading whitespace, making it easier to have sensible looking tests
  expect(expected.replace(/^\s\s*/gm, '')).toBe(actual.replace(/^\s\s*/gm, ''))
}

// Note: resulting JsonLD verified using https://search.google.com/structured-data/testing-tool/u/0/
// this tool requires wrapping the json in <script type="application/ld+json"></script> to validate

describe('In the jsonLdUtils', function(){
  const uuid = 'aabbccdd-1234-5678-9009-87654312abcd'
  const pageUrl =
    'https://sciapps.colorado.edu/onestop/collections/details/aabbccdd-1234-5678-9009-87654312abcd'

  describe('an empty map for input', function(){
    const input = {}

    it('does not break the utility functions', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset"
        }`
      )
    })
  })

  describe('a collection with no optional fields', function(){
    const input = {
      title: 'the title of the record',
      description: 'A rather long description (not!)',
      fileIdentifier: 'gov.test.cires.example:abc',
    }

    it('generates the url block', function(){
      jsonEquals(
        util.urlField(pageUrl),
        `"url": "https://sciapps.colorado.edu/onestop/collections/details/aabbccdd-1234-5678-9009-87654312abcd"`
      )
    })

    it('creates a very simple JSON-LD object', function(){
      jsonEquals(
        util.toJsonLd(uuid, input, pageUrl),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "alternateName": "gov.test.cires.example:abc",
          "description": "A rather long description (not!)",
          "identifier": [
            {
              "value": "aabbccdd-1234-5678-9009-87654312abcd",
              "propertyID": "OneStop UUID",
              "@type": "PropertyValue"
            },
            {
              "value": "gov.test.cires.example:abc",
              "propertyID": "NCEI Dataset Identifier",
              "@type": "PropertyValue"
            }
          ],
          "url": "https://sciapps.colorado.edu/onestop/collections/details/aabbccdd-1234-5678-9009-87654312abcd",
          "sameAs": [
            "https://data.nodc.noaa.gov/cgi-bin/iso?id=gov.test.cires.example:abc"
          ]
        }`
      )
    })

    it('does not generate a doi block', function(){
      expect(util.doiListItem(input)).toBe(undefined)
    })

    it('does not generate a thumbnail image block', function(){
      expect(util.imageField(input)).toBe(undefined)
    })

    it('does not generate a temporal block', function(){
      expect(util.temporalCoverageField(input)).toBe(undefined)
    })

    it('does not generate a spatial block', function(){
      expect(util.spatialCoverageField(input)).toBe(undefined)
    })

    it('does not generate a distribution block', function(){
      expect(util.distributionField(input)).toBe(undefined)
    })

    it('does not generate a keyword block', function(){
      expect(util.keywordsField(input)).toBe(undefined)
    })

    it('does not generate an encoding format block', function(){
      expect(util.encodingFormatField(input)).toBe(undefined)
    })
  })

  describe('a collection with just a fileIdentifier', function(){
    const input = {
      title: 'the title of the record',
      fileIdentifier: 'gov.test.cires.example:abc',
    }

    it('generates an id from the fileIdentifier', function(){
      jsonEquals(
        util.fileIdentifierListItem(input),
        `{
          "value": "gov.test.cires.example:abc",
          "propertyID": "NCEI Dataset Identifier",
          "@type": "PropertyValue"
        }`
      )
    })

    it('generates a simple identifier block', function(){
      jsonEquals(
        util.identifierField(null, input),
        `"identifier": [
          {
            "value": "gov.test.cires.example:abc",
            "propertyID": "NCEI Dataset Identifier",
            "@type": "PropertyValue"
          }
        ]`
      )
    })

    it('generates the sameAs block', function(){
      jsonEquals(
        util.sameAsField(input),
        `"sameAs": [
          "https://data.nodc.noaa.gov/cgi-bin/iso?id=gov.test.cires.example:abc"
        ]`
      )
    })

    it('generates the alternateName block', function(){
      jsonEquals(
        util.alternateNameField(input),
        `"alternateName": "gov.test.cires.example:abc"`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "alternateName": "gov.test.cires.example:abc",
          "identifier": [
            {
              "value": "gov.test.cires.example:abc",
              "propertyID": "NCEI Dataset Identifier",
              "@type": "PropertyValue"
            }
          ],
          "sameAs": [
            "https://data.nodc.noaa.gov/cgi-bin/iso?id=gov.test.cires.example:abc"
          ]
        }`
      )
    })
  })

  describe('a collection with a doi', function(){
    const input = {
      title: 'the title of the record',
      doi: 'doi:10.1234/ABCDEFGH',
    }

    it('generates a doi block', function(){
      jsonEquals(
        util.doiListItem(input),
        `{
          "value": "doi:10.1234/ABCDEFGH",
          "propertyID": "Digital Object Identifier (DOI)",
          "@type": "PropertyValue"
        }`
      )
    })

    it('generates a simple identifier block', function(){
      jsonEquals(
        util.identifierField(null, input),
        `"identifier": [
          {
            "value": "doi:10.1234/ABCDEFGH",
            "propertyID": "Digital Object Identifier (DOI)",
            "@type": "PropertyValue"
          }
        ]`
      )
    })

    it('generates the sameAs block', function(){
      jsonEquals(
        util.sameAsField(input),
        `"sameAs": [
          "https://doi.org/doi:10.1234/ABCDEFGH"
        ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "identifier": [
            {
              "value": "doi:10.1234/ABCDEFGH",
              "propertyID": "Digital Object Identifier (DOI)",
              "@type": "PropertyValue"
            }
          ],
          "sameAs": [
            "https://doi.org/doi:10.1234/ABCDEFGH"
          ]
        }`
      )
    })
  })

  describe('a collection with multiple identifiers', function(){
    const input = {
      title: 'the title of the record',
      fileIdentifier: 'gov.test.cires.example:abc',
      doi: 'doi:10.1234/ABCDEFGH',
    }

    it('generates the identifier block', function(){
      jsonEquals(
        util.identifierField(uuid, input),
        `"identifier": [
          {
            "value": "aabbccdd-1234-5678-9009-87654312abcd",
            "propertyID": "OneStop UUID",
            "@type": "PropertyValue"
          },
          {
            "value": "gov.test.cires.example:abc",
            "propertyID": "NCEI Dataset Identifier",
            "@type": "PropertyValue"
          },
          {
            "value": "doi:10.1234/ABCDEFGH",
            "propertyID": "Digital Object Identifier (DOI)",
            "@type": "PropertyValue"
          }
        ]`
      )
    })

    it('generates the sameAs block', function(){
      jsonEquals(
        util.sameAsField(input),
        `"sameAs": [
          "https://doi.org/doi:10.1234/ABCDEFGH",
          "https://data.nodc.noaa.gov/cgi-bin/iso?id=gov.test.cires.example:abc"
        ]`
      )
    })

    it('generates the url block', function(){
      jsonEquals(
        util.urlField(pageUrl),
        `"url": "https://sciapps.colorado.edu/onestop/collections/details/aabbccdd-1234-5678-9009-87654312abcd"`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(uuid, input, pageUrl),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "alternateName": "gov.test.cires.example:abc",
          "identifier": [
            {
              "value": "aabbccdd-1234-5678-9009-87654312abcd",
              "propertyID": "OneStop UUID",
              "@type": "PropertyValue"
            },
            {
              "value": "gov.test.cires.example:abc",
              "propertyID": "NCEI Dataset Identifier",
              "@type": "PropertyValue"
            },
            {
              "value": "doi:10.1234/ABCDEFGH",
              "propertyID": "Digital Object Identifier (DOI)",
              "@type": "PropertyValue"
            }
          ],
          "url": "https://sciapps.colorado.edu/onestop/collections/details/aabbccdd-1234-5678-9009-87654312abcd",
          "sameAs": [
            "https://doi.org/doi:10.1234/ABCDEFGH",
            "https://data.nodc.noaa.gov/cgi-bin/iso?id=gov.test.cires.example:abc"
          ]
        }`
      )
    })
  })

  describe('a collection with a thumbnail', function(){
    const input = {
      title: 'the title of the record',
      thumbnail: 'http://example.com/thumbnail',
    }

    it('generates an image block', function(){
      jsonEquals(
        util.imageField(input),
        `"image": {
          "@type": "ImageObject",
          "url": "http://example.com/thumbnail",
          "contentUrl": "http://example.com/thumbnail",
          "caption": "Preview graphic"
        }`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "image": {
            "@type": "ImageObject",
            "url": "http://example.com/thumbnail",
            "contentUrl": "http://example.com/thumbnail",
            "caption": "Preview graphic"
          }
        }`
      )
    })
  })

  describe('a collection with a bounded date range', function(){
    const input = {
      title: 'the title of the record',
      beginDate: '2018-10-19',
      endDate: '2019-01-02',
    }

    it('generates an temporal block', function(){
      jsonEquals(
        util.temporalCoverageField(input),
        `"temporalCoverage": "2018-10-19/2019-01-02"`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "temporalCoverage": "2018-10-19/2019-01-02"
        }`
      )
    })
  })

  describe('a collection with an unbounded date range', function(){
    const input = {
      title: 'the title of the record',
      beginDate: '2018-10-19',
    }

    it('generates an temporal block', function(){
      jsonEquals(
        util.temporalCoverageField(input),
        `"temporalCoverage": "2018-10-19/.."`
      )
    })
  })

  describe('a collection with a missing start date', function(){
    const input = {
      title: 'the title of the record',
      endDate: '2018-10-19',
    }

    it('generates an temporal block', function(){
      jsonEquals(
        util.temporalCoverageField(input),
        `"temporalCoverage": "../2018-10-19"`
      )
    })
  })

  describe('a collection with an instant date', function(){
    const input = {
      beginDate: '2018-10-19',
      endDate: '2018-10-19',
    }

    it('generates an temporal block', function(){
      jsonEquals(
        util.temporalCoverageField(input),
        `"temporalCoverage": "2018-10-19"`
      )
    })
  })

  describe('a collection with a bounding box', function(){
    const input = {
      title: 'the title of the record',
      spatialBounding: {
        coordinates: [
          [
            [ -180, 32 ],
            [ -116, 32 ],
            [ -116, 62 ],
            [ -180, 62 ],
            [ -180, 32 ],
          ],
        ],
        type: 'Polygon',
      },
    }

    it('generates a geo shape', function(){
      jsonEquals(
        util.geoListItem(input),
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

    it('generates a spatial block', function(){
      jsonEquals(
        util.spatialCoverageField(input),
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

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
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

  describe('a collection with a line', function(){
    const input = {
      title: 'the title of the record',
      spatialBounding: {
        coordinates: [ [ -7.7, 51.5 ], [ -7.7, 51.6 ] ],
        type: 'LineString',
      },
    }

    it('generates a geo shape', function(){
      jsonEquals(
        util.geoListItem(input),
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

    it('generates a spatial block', function(){
      jsonEquals(
        util.spatialCoverageField(input),
        `"spatialCoverage": [
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

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
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

  describe('a collection with a point', function(){
    const input = {
      title: 'the title of the record',
      spatialBounding: {
        coordinates: [ [ -49.815, 69.222 ] ],
        type: 'Point',
      },
    }

    it('generates a geo shape', function(){
      jsonEquals(
        util.geoListItem(input),
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

    it('generates a spatial block', function(){
      jsonEquals(
        util.spatialCoverageField(input),
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

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
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

  describe('a collection with a polygon', function(){
    const input = {
      title: 'the title of the record',
      spatialBounding: {
        coordinates: [
          [ [ 1, 2 ], [ 3, 4 ], [ 5, 6 ], [ 7, 8 ], [ 9, 10 ], [ 1, 2 ] ],
        ],
        type: 'Polygon',
      },
    }

    it('generates a geo shape', function(){
      jsonEquals(
        util.geoListItem(input),
        `{
          "@type": "Place",
          "name": "geographic polygon",
          "geo": {
            "@type": "GeoShape",
            "description": "y,x y,x ...",
            "polygon": "2,1 4,3 6,5 8,7 10,9 2,1"
          }
        }`
      )
    })

    it('generates a spatial block', function(){
      jsonEquals(
        util.spatialCoverageField(input),
        `"spatialCoverage": [
          {
            "@type": "Place",
            "name": "geographic polygon",
            "geo": {
              "@type": "GeoShape",
              "description": "y,x y,x ...",
              "polygon": "2,1 4,3 6,5 8,7 10,9 2,1"
            }
          }
        ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "spatialCoverage": [
            {
              "@type": "Place",
              "name": "geographic polygon",
              "geo": {
                "@type": "GeoShape",
                "description": "y,x y,x ...",
                "polygon": "2,1 4,3 6,5 8,7 10,9 2,1"
              }
            }
          ]
        }`
      )
    })
  })

  describe('a collection with a multipolygon', function(){
    const input = {
      title: 'the title of the record',
      spatialBounding: {
        coordinates: [
          [
            [
              //bbox
              [ -180, -81.3282 ],
              [ 6.2995, -81.3282 ],
              [ 6.2995, 81.3282 ],
              [ -180, 81.3282 ],
              [ -180, -81.3282 ],
            ],
          ],
          [
            [
              // 5 sides
              [ 141.7005, -81.3282 ],
              [ 180, -81.3282 ],
              [ 180, 81.3282 ],
              [ 141.7005, 81.3282 ],
              [ 131.5429, 0 ],
              [ 141.7005, -81.3282 ],
            ],
          ],
        ],
        type: 'MultiPolygon',
      },
    }

    it('generates a geo shape', function(){
      jsonEquals(
        util.geoListItem(input),
        `{
            "@type": "Place",
            "name": "geographic bounding box",
            "geo": {
              "@type": "GeoShape",
              "description": "minY,minX maxY,maxX",
              "box": "-81.3282,-180 81.3282,6.2995"
            }
          }, {
            "@type": "Place",
            "name": "geographic polygon",
            "geo": {
              "@type": "GeoShape",
              "description": "y,x y,x ...",
              "polygon": "-81.3282,141.7005 -81.3282,180 81.3282,180 81.3282,141.7005 0,131.5429 -81.3282,141.7005"
            }
          }`
      )
    })

    it('generates a spatial block', function(){
      jsonEquals(
        util.spatialCoverageField(input),
        `"spatialCoverage": [
            {
              "@type": "Place",
              "name": "geographic bounding box",
              "geo": {
                "@type": "GeoShape",
                "description": "minY,minX maxY,maxX",
                "box": "-81.3282,-180 81.3282,6.2995"
              }
            }, {
              "@type": "Place",
              "name": "geographic polygon",
              "geo": {
                "@type": "GeoShape",
                "description": "y,x y,x ...",
                "polygon": "-81.3282,141.7005 -81.3282,180 81.3282,180 81.3282,141.7005 0,131.5429 -81.3282,141.7005"
              }
            }
          ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
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
                  "box": "-81.3282,-180 81.3282,6.2995"
                }
              }, {
                "@type": "Place",
                "name": "geographic polygon",
                "geo": {
                  "@type": "GeoShape",
                  "description": "y,x y,x ...",
                  "polygon": "-81.3282,141.7005 -81.3282,180 81.3282,180 81.3282,141.7005 0,131.5429 -81.3282,141.7005"
                }
              }
            ]
          }`
      )
    })
  })

  it('generates keyword places', function(){
    const testCases = [
      {
        input: 'Continent > North America > United States Of America',
        output: `{
          "@type": "Place",
          "name": "Continent > North America > United States Of America"
        }`,
      },
      {
        input: 'Ocean > Pacific Ocean > North Pacific Ocean',
        output: `{
          "@type": "Place",
          "name": "Ocean > Pacific Ocean > North Pacific Ocean"
        }`,
      },
      {
        input: 'Vertical Location > Land Surface',
        output: `{
          "@type": "Place",
          "name": "Vertical Location > Land Surface"
        }`,
      },
      {
        input: 'Vertical Location > Sea Floor',
        output: `{
          "@type": "Place",
          "name": "Vertical Location > Sea Floor"
        }`,
      },
    ]

    testCases.forEach(c => {
      jsonEquals(util.placenameListItem(c.input), c.output)
    })
  })

  describe('a collection with gcmdLocations', function(){
    const input = {
      title: 'the title of the record',
      gcmdLocations: [
        'Continent > North America > United States Of America',
        'Continent > North America',
        'Continent',
        'Ocean > Pacific Ocean > North Pacific Ocean',
        'Ocean > Pacific Ocean',
        'Ocean',
        'Vertical Location > Land Surface',
        'Vertical Location',
        'Vertical Location > Sea Floor',
      ],
      keywords: [
        'Oceans > Bathymetry/Seafloor Topography > Water Depth',
        'Land Surface > Topography > Terrain Elevation',
        'Integrated bathymetry and topography',
        'Continent > North America > United States Of America',
        'Ocean > Pacific Ocean > North Pacific Ocean',
        'Vertical Location > Land Surface',
        'Vertical Location > Sea Floor',
        'DOC/NOAA/NESDIS/NGDC > National Geophysical Data Center, NESDIS, NOAA, U.S. Department of Commerce',
      ],
    }

    it('identifies the correct place keywords', function(){
      expect(util.locationKeywordsSubset(input)).toEqual([
        'Continent > North America > United States Of America',
        'Ocean > Pacific Ocean > North Pacific Ocean',
        'Vertical Location > Land Surface',
        'Vertical Location > Sea Floor',
      ])
    })

    it('generates a spatial block', function(){
      jsonEquals(
        util.spatialCoverageField(input),
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

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
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

  describe('a collection with keywords', function(){
    const input = {
      title: 'the title of the record',
      keywords: [
        'Oceans > Bathymetry/Seafloor Topography > Seafloor Topography',
        'Oceans > Bathymetry/Seafloor Topography > Bathymetry',
        'Oceans > Bathymetry/Seafloor Topography > Water Depth',
        'Land Surface > Topography > Terrain Elevation',
        'Land Surface > Topography > Terrain Elevation > Topographical Relief Maps',
        'Oceans > Coastal Processes > Coastal Elevation',
        'Models/Analyses > DEM > Digital Elevation Model',
        'ICSU-WDS > International Council for Science - World Data System',
        '< 1 meter',
        'Coastal Relief',
        'Gridded elevations',
        'Integrated bathymetry and topography',
        'Continent > North America > United States Of America',
        'Ocean > Pacific Ocean > North Pacific Ocean',
        'Vertical Location > Land Surface',
        'Vertical Location > Sea Floor',
        'DOC/NOAA/NESDIS/NCEI > National Centers for Environmental Information, NESDIS, NOAA, U.S. Department of Commerce',
        'DOC/NOAA/NESDIS/NGDC > National Geophysical Data Center, NESDIS, NOAA, U.S. Department of Commerce',
      ],
      gcmdScience: [
        'Oceans',
        'Oceans > Bathymetry/Seafloor Topography',
        'Oceans > Bathymetry/Seafloor Topography > Seafloor Topography',
        'Oceans > Bathymetry/Seafloor Topography > Bathymetry',
        'Oceans > Bathymetry/Seafloor Topography > Water Depth',
        'Land Surface',
        'Land Surface > Topography',
        'Land Surface > Topography > Terrain Elevation',
        'Land Surface > Topography > Terrain Elevation > Topographical Relief Maps',
        'Oceans',
        'Oceans > Coastal Processes',
        'Oceans > Coastal Processes > Coastal Elevation',
      ],
    }

    it('generates a keyword block', function(){
      jsonEquals(
        util.keywordsField(input),
        `"keywords": [
          "Oceans > Bathymetry/Seafloor Topography > Seafloor Topography",
          "Oceans > Bathymetry/Seafloor Topography > Bathymetry",
          "Oceans > Bathymetry/Seafloor Topography > Water Depth",
          "Land Surface > Topography > Terrain Elevation",
          "Land Surface > Topography > Terrain Elevation > Topographical Relief Maps",
          "Oceans > Coastal Processes > Coastal Elevation"
        ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "keywords": [
            "Oceans > Bathymetry/Seafloor Topography > Seafloor Topography",
            "Oceans > Bathymetry/Seafloor Topography > Bathymetry",
            "Oceans > Bathymetry/Seafloor Topography > Water Depth",
            "Land Surface > Topography > Terrain Elevation",
            "Land Surface > Topography > Terrain Elevation > Topographical Relief Maps",
            "Oceans > Coastal Processes > Coastal Elevation"
          ]
        }`
      )
    })
  })

  it('generates download links', function(){
    const testCases = [
      {
        input: {
          linkUrl: 'http://example.com/download',
          linkDescription: 'an example link',
          linkName: 'get data here',
          linkProtocol: 'http',
          linkFunction: 'information',
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "description": "an example link",
          "disambiguatingDescription": "information (http)",
          "name": "get data here",
          "encodingFormat": "http"
        }`,
      },
      {
        input: {
          linkUrl: 'http://example.com/download',
          linkName: 'get data here',
          linkProtocol: 'http',
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "disambiguatingDescription": "download (http)",
          "name": "get data here",
          "encodingFormat": "http"
        }`,
      },
      {
        input: {
          linkUrl: 'http://example.com/download',
          linkDescription: 'an example link',
          linkProtocol: 'http',
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "description": "an example link",
          "disambiguatingDescription": "download (http)",
          "encodingFormat": "http"
        }`,
      },
      {
        input: {
          linkUrl: 'http://example.com/download',
          linkDescription: 'an example link',
          linkName: 'get data here',
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "description": "an example link",
          "disambiguatingDescription": "download (HTTP)",
          "name": "get data here"
        }`,
      },
      {
        input: {
          linkUrl: 'http://example.com/download',
        },
        output: `{
          "@type": "DataDownload",
          "url": "http://example.com/download",
          "disambiguatingDescription": "download (HTTP)"
        }`,
      },
    ]

    testCases.forEach(c => {
      jsonEquals(util.downloadLinkList(c.input), c.output)
    })
  })

  describe('a collection with download links', function(){
    const input = {
      title: 'the title of the record',
      links: [
        {
          linkUrl: 'http://example.com/download_1',
          linkDescription: 'an example link',
          linkName: 'get data here',
          linkProtocol: 'http',
          linkFunction: 'download',
        },
        {
          linkUrl: 'http://example.com/download_2',
          linkDescription: 'another link',
          linkName: 'cloud',
          linkProtocol: 's3',
          linkFunction: 'download',
        },
      ],
    }

    it('generates a distribution block', function(){
      jsonEquals(
        util.distributionField(input),
        `"distribution": [
          {
            "@type": "DataDownload",
            "url": "http://example.com/download_1",
            "description": "an example link",
            "disambiguatingDescription": "download (http)",
            "name": "get data here",
            "encodingFormat": "http"
          },
          {
            "@type": "DataDownload",
            "url": "http://example.com/download_2",
            "description": "another link",
            "disambiguatingDescription": "download (s3)",
            "name": "cloud",
            "encodingFormat": "s3"
          }
        ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "distribution": [
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_1",
              "description": "an example link",
              "disambiguatingDescription": "download (http)",
              "name": "get data here",
              "encodingFormat": "http"
            },
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_2",
              "description": "another link",
              "disambiguatingDescription": "download (s3)",
              "name": "cloud",
              "encodingFormat": "s3"
            }
          ]
        }`
      )
    })
  })

  describe('a collection with information links', function(){
    const input = {
      title: 'the title of the record',
      links: [
        {
          linkUrl: 'http://example.com/help',
          linkDescription: 'helpful info',
          linkName: 'info page',
          linkProtocol: 'http',
          linkFunction: 'information',
        },
      ],
    }

    it('generates a distribution block', function(){
      jsonEquals(
        util.distributionField(input),
        `"distribution": [
          {
            "@type": "DataDownload",
            "url": "http://example.com/help",
            "description": "helpful info",
            "disambiguatingDescription": "information (http)",
            "name": "info page",
            "encodingFormat": "http"
          }
        ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "distribution": [
            {
              "@type": "DataDownload",
              "url": "http://example.com/help",
              "description": "helpful info",
              "disambiguatingDescription": "information (http)",
              "name": "info page",
              "encodingFormat": "http"
            }
          ]
        }`
      )
    })
  })

  describe('a collection with dataFormats', function(){
    const input = {
      title: 'the title of the record',
      dataFormats: [
        {
          name: 'NETCDF',
          version: 'netCDF-4 Classic',
        },
        {
          name: 'FITS',
          version: null,
        },
      ],
    }

    it('generates an encodingFormat block', function(){
      jsonEquals(
        util.encodingFormatField(input),
        `"encodingFormat": [
          "NETCDF netCDF-4 Classic",
          "FITS"
        ]`
      )
    })

    it('generates json-ld', function(){
      jsonEquals(
        util.toJsonLd(null, input),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "encodingFormat": [
            "NETCDF netCDF-4 Classic",
            "FITS"
          ]
        }`
      )
    })
  })

  describe('a complete collection', function(){
    const input = {
      title: 'the title of the record',
      description: 'A rather long description (not!)',
      fileIdentifier: 'gov.test.cires.example:abc',
      doi: 'doi:10.1234/ABCDEFGH',
      thumbnail: 'http://example.com/thumbnail',
      beginDate: '2018-10-19',
      endDate: '2019-01-02',
      spatialBounding: {
        coordinates: [
          [
            [ -180, -90 ],
            [ 180, -90 ],
            [ 180, 90 ],
            [ -180, 90 ],
            [ -180, -90 ],
          ],
        ],
        type: 'Polygon',
      },
      gcmdLocations: [
        'Continent > North America > United States Of America',
        'Vertical Location > Sea Floor',
      ],
      gcmdScience: [
        'Oceans',
        'Oceans > Bathymetry/Seafloor Topography',
        'Oceans > Bathymetry/Seafloor Topography > Water Depth',
      ],
      keywords: [
        'Oceans > Bathymetry/Seafloor Topography > Water Depth',
        'Continent > North America > United States Of America',
        'Vertical Location > Sea Floor',
      ],
      links: [
        {
          linkUrl: 'http://example.com/download_1',
          linkDescription: 'an example link',
          linkName: 'get data here',
          linkProtocol: 'http',
          linkFunction: 'download',
        },
        {
          linkUrl: 'http://example.com/download_2',
          linkDescription: 'another link',
          linkName: 'cloud',
          linkProtocol: 's3',
          linkFunction: 'download',
        },
        {
          linkUrl: 'http://example.com/help',
          linkDescription: 'helpful info',
          linkName: 'info page',
          linkProtocol: 'http',
          linkFunction: 'information',
        },
      ],
      dataFormats: [
        {
          name: 'NETCDF',
          version: 'netCDF-4 Classic',
        },
      ],
    }

    it('generates full json-ld', function(){
      jsonEquals(
        util.toJsonLd(uuid, input, pageUrl),
        `{
          "@context": "http://schema.org",
          "@type": "Dataset",
          "name": "the title of the record",
          "alternateName": "gov.test.cires.example:abc",
          "description": "A rather long description (not!)",
          "identifier": [
            {
              "value": "aabbccdd-1234-5678-9009-87654312abcd",
              "propertyID": "OneStop UUID",
              "@type": "PropertyValue"
            },
            {
              "value": "gov.test.cires.example:abc",
              "propertyID": "NCEI Dataset Identifier",
              "@type": "PropertyValue"
            },
            {
              "value": "doi:10.1234/ABCDEFGH",
              "propertyID": "Digital Object Identifier (DOI)",
              "@type": "PropertyValue"
            }
          ],
          "url": "https://sciapps.colorado.edu/onestop/collections/details/aabbccdd-1234-5678-9009-87654312abcd",
          "sameAs": [
            "https://doi.org/doi:10.1234/ABCDEFGH",
            "https://data.nodc.noaa.gov/cgi-bin/iso?id=gov.test.cires.example:abc"
          ],
          "image": {
            "@type": "ImageObject",
            "url": "http://example.com/thumbnail",
            "contentUrl": "http://example.com/thumbnail",
            "caption": "Preview graphic"
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
              "disambiguatingDescription": "download (http)",
              "name": "get data here",
              "encodingFormat": "http"
            },
            {
              "@type": "DataDownload",
              "url": "http://example.com/download_2",
              "description": "another link",
              "disambiguatingDescription": "download (s3)",
              "name": "cloud",
              "encodingFormat": "s3"
            },
            {
              "@type": "DataDownload",
              "url": "http://example.com/help",
              "description": "helpful info",
              "disambiguatingDescription": "information (http)",
              "name": "info page",
              "encodingFormat": "http"
            }
          ],
          "keywords": [
            "Oceans > Bathymetry/Seafloor Topography > Water Depth"
          ],
          "encodingFormat": [
            "NETCDF netCDF-4 Classic"
          ]
        }`
      )
    })
  })

  it('produces search action for the root page', function(){
    const rootUrl = 'https://sciapps.colorado.edu/onestop/'

    jsonEquals(
      util.appJsonLd(rootUrl),
      `{
        "@context": "http://schema.org",
        "@type": "WebSite",
        "@id": "https://sciapps.colorado.edu/onestop/",
        "url": "https://sciapps.colorado.edu/onestop/",
        "potentialAction": {
          "@type": "SearchAction",
          "target": "https://sciapps.colorado.edu/onestop/collections?q={search_term_string}",
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
  })
})
