import Immutable from 'seamless-immutable'
import {request, initialState} from '../../../src/reducers/behavior/request'
import {
  fetchingGranules,
  fetchedGranules,
} from '../../../src/actions/SearchRequestActions'
import {
  startSearch,
  completeSearch,
} from '../../../src/actions/SearchRequestActions'

describe('The request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = request(initialState, initialAction)

    expect(result.collectionInFlight).toBe(false)
    expect(result.granuleInFlight).toBe(false)
  })

  describe('marks collectionInFlight', function(){
    it('true when retrieving collections', function(){
      const initial = Immutable({collectionInFlight: false})
      const result = request(initial, startSearch())
      expect(result.collectionInFlight).toBe(true)
    })

    it('false when receiving collections', function(){
      const initial = Immutable({collectionInFlight: true})
      const result = request(initial, completeSearch([ {id: 'A'} ]))
      expect(result.collectionInFlight).toBe(false)
    })
  })

  describe('marks granuleInFlight', function(){
    it('true when retrieving granules', function(){
      const initial = Immutable({granuleInFlight: false})
      const result = request(initial, fetchingGranules())
      expect(result.granuleInFlight).toBe(true)
    })

    it('false when receiving granules', function(){
      const initial = Immutable({granuleInFlight: true})
      const result = request(initial, fetchedGranules([ {id: 'A'} ]))
      expect(result.granuleInFlight).toBe(false)
    })
  })
})
