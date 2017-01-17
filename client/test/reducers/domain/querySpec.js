import '../../specHelper'
import { query, initialState } from '../../../src/reducers/domain/query'
import { updateQuery, generateCollectionsQuery } from '../../../src/search/SearchActions'
import { startDate, endDate } from '../../../src/search/temporal/TemporalActions'
import { newGeometry } from '../../../src/search/map/MapActions'

describe('The query reducer', function() {

  it('sets queries array with queryText', function () {
    const queryText = 'DEM'
    const updateQueryAction = updateQuery(queryText)
    const updatedState = query(initialState, updateQueryAction)
    const result = query(updatedState, generateCollectionsQuery())

    const expectedRequestBody = JSON.stringify({queries: [{type: 'queryText', value: 'DEM'}], filters: [], facets: true})

    result.queryText.should.equal('DEM')
    result.formatted.should.equal(expectedRequestBody)
  })

  it('sets filters array with start date only', function () {
    const startDatetime = '2010-07-25T15:45:00-06:00'
    const updateStartDateAction = startDate(startDatetime)
    const updatedState = query(initialState, updateStartDateAction)
    const result = query(updatedState, generateCollectionsQuery())

    const expectedRequestBody = JSON.stringify({queries: [], filters: [{type: 'datetime', after: '2010-07-25T15:45:00-06:00'}], facets: true})

    result.startDateTime.should.equal('2010-07-25T15:45:00-06:00')
    result.formatted.should.equal(expectedRequestBody)
  })

  it('sets filters array with end date only', function () {
    const endDatetime = '2016-07-25T15:45:00-06:00'
    const updateEndDateAction = endDate(endDatetime)
    const updatedState = query(initialState, updateEndDateAction)
    const result = query(updatedState, generateCollectionsQuery())

    const expectedRequestBody = JSON.stringify({queries: [], filters: [{type: 'datetime', before: '2016-07-25T15:45:00-06:00'}], facets: true})

    result.endDateTime.should.equal('2016-07-25T15:45:00-06:00')
    result.formatted.should.equal(expectedRequestBody)
  })

  it('sets filters array with start and end dates', function () {
    const startDatetime = '2010-07-25T15:45:00-06:00'
    const updateStartDateAction = startDate(startDatetime)
    const intermediateResult = query(initialState, updateStartDateAction)

    const endDatetime = '2016-07-25T15:45:00-06:00'
    const updateEndDateAction = endDate(endDatetime)
    const updatedState = query(intermediateResult, updateEndDateAction)
    const result = query(updatedState, generateCollectionsQuery())

    const expectedRequestBody = JSON.stringify({
      queries: [],
      filters: [{type: 'datetime', after: '2010-07-25T15:45:00-06:00', before: '2016-07-25T15:45:00-06:00'}],
      facets: true
    })

    result.startDateTime.should.equal('2010-07-25T15:45:00-06:00')
    result.endDateTime.should.equal('2016-07-25T15:45:00-06:00')
    result.formatted.should.equal(expectedRequestBody)
  })

  it('sets filters array with geometry', function () {
    const validGeoJSON = {
      type: 'Feature',
      geometry: {
        type: 'Polygon',
        coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
      },
      properties: {
        description: 'Valid test GeoJSON'
      }
    }

    const updateGeometryAction = newGeometry(validGeoJSON)
    const updatedState = query(initialState, updateGeometryAction)
    const result = query(updatedState, generateCollectionsQuery())

    const expectedRequestBody = JSON.stringify({
      queries: [],
      filters: [{
        type: 'geometry',
        geometry: {
          type: 'Polygon',
          coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
        }
      }],
      facets: true
    })

    JSON.stringify(result.geoJSON).should.equal(JSON.stringify(validGeoJSON.geometry))
    result.formatted.should.equal(expectedRequestBody)
  })
})
