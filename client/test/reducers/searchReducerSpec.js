import '../specHelper'
import { search, initialState } from '../../src/search/SearchReducer'
import { startSearch, completeSearch } from '../../src/search/SearchActions'

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
