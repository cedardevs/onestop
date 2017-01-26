import '../../specHelper'
import Immutable from 'seamless-immutable'
import { request, initialState } from '../../../src/reducers/behavior/request'
import { fetchingGranules, fetchedGranules } from '../../../src/result/granules/GranulesActions'
import { startSearch, completeSearch } from '../../../src/search/SearchActions'

describe('The request reducer', function () {

  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = request(initialState, initialAction)

    result.collectionInFlight.should.equal(false)
    result.granuleInFlight.should.equal(false)
  })

  describe('marks collectionInFlight', function () {
    it('true when retrieving collections', function () {
      const initial = Immutable({collectionInFlight: false})
      const result = request(initial, startSearch())
      result.collectionInFlight.should.equal(true)
    })

    it('false when receiving collections', function () {
      const initial = Immutable({collectionInFlight: true})
      const result = request(initial, completeSearch([{id: 'A'}]))
      result.collectionInFlight.should.equal(false)
    })
  })

  describe('marks granuleInFlight', function () {
    it('true when retrieving granules', function () {
      const initial = Immutable({granuleInFlight: false})
      const result = request(initial, fetchingGranules())
      result.granuleInFlight.should.equal(true)
    })

    it('false when receiving granules', function () {
      const initial = Immutable({granuleInFlight: true})
      const result = request(initial, fetchedGranules([{id: 'A'}]))
      result.granuleInFlight.should.equal(false)
    })
  })

})
