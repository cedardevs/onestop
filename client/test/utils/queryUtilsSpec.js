import '../specHelper'
import * as queryUtils from '../../src/utils/queryUtils'

describe('The queryUtils', function () {

  testCases().forEach(function (testCase) {
    it(`can assemble requests with ${testCase.name}`, function () {
      const objectResult = queryUtils.assembleSearchRequest(testCase.inputState)
      const stringResult = queryUtils.assembleSearchRequestString(testCase.inputState)
      objectResult.should.deep.equal(testCase.expectedResult)
      stringResult.should.equal(JSON.stringify(testCase.expectedResult))
    })
  })

})

function testCases() {
  return [
    {
      name: "defaults",
      inputState: {},
      expectedResult: {
        queries: [],
        filters: [],
        facets: true
      }
    },
    {
      name: "a text search",
      inputState: {
        appState: {
          queryText: {
            text: "test text"
          }
        }
      },
      expectedResult: {
        queries: [
          {
            type: "queryText", value: "test text"
          }
        ],
        filters: [],
        facets: true
      }
    },
    {
      name: "a temporal search",
      inputState: {
        appState: {
          temporal: {
            startDateTime: "2017-01-01",
            endDateTime: "2017-01-20"
          }
        }
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: "datetime",
            after: "2017-01-01",
            before: "2017-01-20"
          }
        ],
        facets: true
      }
    },
    {
      name: "a spatial search",
      inputState: {
        appState: {
          geometry: {
            geoJSON: {
              geometry: {
                type: 'Polygon',
                coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
              },
              properties: {
                description: 'Valid test GeoJSON'
              }
            }
          }
        }
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: "geometry",
            geometry: {
              type: 'Polygon',
              coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
            }
          }
        ],
        facets: true
      }
    },
    {
      name: "a facet search",
      inputState: {
        appState: {
          facets: {
            selectedFacets: {
              science: ["Atmosphere"]
            }
          }
        }
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: "facet",
            name: "science",
            values: ["Atmosphere"]
          }
        ],
        facets: true
      }
    },
    {
      name: "all filters applied",
      inputState: {
        appState: {
          geometry: {
            geoJSON: {
              geometry: {
                type: 'Polygon',
                coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
              },
              properties: {
                description: 'Valid test GeoJSON'
              }
            }
          },
          facets: {
            selectedFacets: {
              science: ["Atmosphere"]
            }
          },
          temporal: {
            startDateTime: "2017-01-01",
            endDateTime: "2017-01-20"
          },
          queryText: {
            text: "test text"
          }
        }
      },
      expectedResult: {
        queries: [
          {
            type: "queryText", value: "test text"
          }
        ],
        filters: [
          {
            type: "facet",
            name: "science",
            values: ["Atmosphere"]
          },
          {
            type: "geometry",
            geometry: {
              type: 'Polygon',
              coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
            }
          },
          {
            type: "datetime",
            after: "2017-01-01",
            before: "2017-01-20"
          }
        ],
        facets: true
      }
    }
  ]
}
