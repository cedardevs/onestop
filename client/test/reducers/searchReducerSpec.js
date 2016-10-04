import '../specHelper'
import { search, initialState } from '../../src/search/SearchReducer'
import { updateQuery, startSearch, completeSearch } from '../../src/search/SearchActions'
import { startDate, endDate } from '../../src/search/temporal/TemporalActions'
import { newGeometry } from '../../src/search/map/MapActions'

describe('The search reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = search(initialState, initialAction)

    result.has('text').should.equal(true)
  })

  it('for a new search', function () {
    const searchAction = startSearch()
    const result = search(initialState, searchAction)

    result.should.not.equal(initialState)
    result.get('inFlight').should.equal(true)
  })

  it('for a completed search', function () {
    const searchResults = [{id: '1'}, {id: '2'}]
    const searchAction = completeSearch(searchResults)
    const result = search(initialState, searchAction)

    result.get('inFlight').should.equal(false)
  })
})

describe('The search reducer\'s assembleRequestBody function', function() {

  it('sets queries array with queryText', function () {
    const queryText = 'DEM'
    const updateQueryAction = updateQuery(queryText)
    const result = search(initialState, updateQueryAction)

    const expectedRequestBody = JSON.stringify({queries: [{type: 'queryText', value: 'DEM'}], filters: [], facets: true})

    result.get('text').should.equal('DEM')
    result.get('requestBody').should.equal(expectedRequestBody)
  })

  it('sets filters array with start date only', function () {
    const startDatetime = '2010-07-25T15:45:00-06:00'
    const updateStartDateAction = startDate(startDatetime)
    const result = search(initialState, updateStartDateAction)

    const expectedRequestBody = JSON.stringify({queries: [], filters: [{type: 'datetime', after: '2010-07-25T15:45:00-06:00'}], facets: true})

    result.get('startDateTime').should.equal('2010-07-25T15:45:00-06:00')
    result.get('requestBody').should.equal(expectedRequestBody)
  })

  it('sets filters array with end date only', function () {
    const endDatetime = '2016-07-25T15:45:00-06:00'
    const updateEndDateAction = endDate(endDatetime)
    const result = search(initialState, updateEndDateAction)

    const expectedRequestBody = JSON.stringify({queries: [], filters: [{type: 'datetime', before: '2016-07-25T15:45:00-06:00'}], facets: true})

    result.get('endDateTime').should.equal('2016-07-25T15:45:00-06:00')
    result.get('requestBody').should.equal(expectedRequestBody)
  })

  it('sets filters array with start and end dates', function () {
    const startDatetime = '2010-07-25T15:45:00-06:00'
    const updateStartDateAction = startDate(startDatetime)
    const intermediateResult = search(initialState, updateStartDateAction)

    const endDatetime = '2016-07-25T15:45:00-06:00'
    const updateEndDateAction = endDate(endDatetime)
    const result = search(intermediateResult, updateEndDateAction)

    const expectedRequestBody = JSON.stringify({
      queries: [],
      filters: [{type: 'datetime', after: '2010-07-25T15:45:00-06:00', before: '2016-07-25T15:45:00-06:00'}],
      facets: true
    })

    result.get('startDateTime').should.equal('2010-07-25T15:45:00-06:00')
    result.get('endDateTime').should.equal('2016-07-25T15:45:00-06:00')
    result.get('requestBody').should.equal(expectedRequestBody)
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
    const result = search(initialState, updateGeometryAction)

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

    JSON.stringify(result.get('geoJSON')).should.equal(JSON.stringify(validGeoJSON))
    result.get('requestBody').should.equal(expectedRequestBody)
  })
})
