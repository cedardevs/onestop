import '../../specHelper'
import { collectionRequest, initialState } from '../../../src/reducers/behavior/collectionRequest'
import { startSearch, completeSearch } from '../../../src/search/SearchActions'

describe('The collectionRequest reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = collectionRequest(initialState, initialAction)

    result.inFlight.should.equal(false)
  })

  it('for a new search', function () {
    const searchAction = startSearch()
    const result = collectionRequest(initialState, searchAction)

    result.should.not.equal(initialState)
    result.inFlight.should.equal(true)
  })

  it('for a completed search', function () {
    const searchResults = [{id: '1'}, {id: '2'}]
    const searchAction = completeSearch(searchResults)
    const result = collectionRequest(initialState, searchAction)

    result.inFlight.should.equal(false)
  })
})
