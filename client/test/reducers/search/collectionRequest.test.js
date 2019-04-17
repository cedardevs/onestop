import Immutable from 'seamless-immutable'
import {
  collectionRequest,
  initialState,
} from '../../../src/reducers/search/collectionRequest'
import {
  collectionSearchRequest,
  collectionSearchSuccess,
  collectionDetailGranulesRequest,
  collectionDetailGranulesSuccess,
} from '../../../src/actions/search/CollectionRequestActions'

describe('The request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionRequest(initialState, initialAction)

    expect(result.collectionSearchRequestInFlight).toBe(false)
    expect(result.collectionDetailGranulesRequestInFlight).toBe(false)
  })

  describe('marks collectionSearchRequestInFlight', function(){
    it('true when retrieving collections', function(){
      const initial = Immutable({collectionSearchRequestInFlight: false})
      const result = collectionRequest(initial, collectionSearchRequest())
      expect(result.collectionSearchRequestInFlight).toBe(true)
    })

    it('false when receiving collections', function(){
      const initial = Immutable({collectionSearchRequestInFlight: true})
      const result = collectionRequest(
        initial,
        collectionSearchSuccess([ {id: 'A'} ])
      )
      expect(result.collectionSearchRequestInFlight).toBe(false)
    })
  })

  describe('marks collectionDetailGranulesRequestInFlight', function(){
    it('true when retrieving granules', function(){
      const initial = Immutable({
        collectionDetailGranulesRequestInFlight: false,
      })
      const result = collectionRequest(
        initial,
        collectionDetailGranulesRequest()
      )
      expect(result.collectionDetailGranulesRequestInFlight).toBe(true)
    })

    it('false when receiving granules', function(){
      const initial = Immutable({collectionDetailGranulesRequestInFlight: true})
      const result = collectionRequest(
        initial,
        collectionDetailGranulesSuccess([ {id: 'A'} ])
      )
      expect(result.collectionDetailGranulesRequestInFlight).toBe(false)
    })
  })
})
