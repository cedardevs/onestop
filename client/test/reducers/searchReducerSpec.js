import Immutable from 'immutable'
import '../specHelper'
import { search, initialState } from '../../src/search/SearchReducer'
import { dateTime,  startSearch, completeSearch } from '../../src/search/SearchActions'
import moment from 'moment'

describe('The search reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = search(initialState, initialAction)

    result.has('text').should.equal(true)
  })

  it('for a new search', function () {
    const searchText = 'test'
    const searchAction = startSearch(searchText)
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

describe.skip('The search reducer\'s assembleSearchBody function', function() {

  it('sets queries array with queryText', function() {})

  it('sets filters array with start date only', function() {})

  it('sets filters array with end date only', function() {})

  it('sets filters array with start and end dates', function() {})

  it('sets filters array with geometry', function() {})
})

/*describe.skip('The search reducer\'s temporal test cases', function () {

  it(' update search with requested datetime ', function () {
    const startDateTime = moment('2000-07-30 00:00').format()
    const endDateTime = moment('2014-07-30 00:00').format()

    const expected = { type: 'datetime', after: startDateTime, before: endDateTime }
    const searchAction = dateTime(startDateTime, endDateTime)

    searchAction.should.deep.equal(expected)

  })

  it(' update search with requested start datetime ', function () {
    const startDateTime = moment('2000-07-30 00:00').format()
    const endDateTime = ""
    const defaultDate = moment().format()

    const expected = { type: 'datetime', after: startDateTime, before: defaultDate }
    const searchAction = dateTime(startDateTime, endDateTime)

    searchAction.should.deep.equal(expected)
  })

  it(' update search with requested end datetime ', function () {
    const startDateTime = ""
    const endDateTime = moment('2000-07-30 00:00').format()

    const expected = { type: 'datetime', before: endDateTime }
    const searchAction = dateTime(startDateTime, endDateTime)

    searchAction.should.deep.equal(expected)
  })

  it(' update search with default datetime ', function () {
    const startDateTime = ""
    const endDateTime = ""
    const defaultDate = moment().format()

    const searchAction = dateTime(startDateTime, endDateTime)
    const expected = { type: 'datetime', before: defaultDate }

    searchAction.should.deep.equal(expected)
  })
})

describe.skip('The search reducer\'s geometry test cases', function () {

  const validState = Immutable.Map({
    text: '',
    geoJSON: Immutable.Map({
      "type": "Feature",
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
        ]
      },
      "properties": {
        "description": "Valid test GeoJSON"
      }
    }),
    inFlight: false,
    startDateTime: '',
    endDateTime: ''
  })

  it('handles a valid geoJSON object', function () {
    // TODO
  })


  const invalidState = Immutable.Map({
    text: '',
    geoJSON: Immutable.Map({
      "type": "Feature",
      "properties": {
        "description": "Valid test GeoJSON"
      }
    }),
    inFlight: false,
    startDateTime: '',
    endDateTime: ''
  })

  it('explodes(?) with an invalid geoJSON object', function() {
    // TODO
  })
})*/
