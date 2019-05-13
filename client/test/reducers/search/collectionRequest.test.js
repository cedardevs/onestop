import Immutable from 'seamless-immutable'
import {
  collectionRequest,
  initialState,
} from '../../../src/reducers/search/collectionRequest'
import {
  collectionNewSearchRequested,
  collectionMoreResultsRequested,
  collectionNewSearchResultsRecieved,
  collectionMoreResultsRecieved,
  collectionSearchError,
} from '../../../src/actions/search/CollectionRequestActions'

describe('The collection request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionRequest(initialState, initialAction)

    expect(result).toEqual({collectionSearchRequestInFlight: false})
  })

  describe('updates in-flight value', function(){
    it('when starting a new search', function(){
      const initial = Immutable({collectionSearchRequestInFlight: false})
      const result = collectionRequest(initial, collectionNewSearchRequested())
      expect(result.collectionSearchRequestInFlight).toBeTruthy()
    })
    it('when starting a new page request', function(){
      const initial = Immutable({collectionSearchRequestInFlight: false})
      const result = collectionRequest(
        initial,
        collectionMoreResultsRequested()
      )
      expect(result.collectionSearchRequestInFlight).toBeTruthy()
    })

    it('when new search completes', function(){
      const initial = Immutable({collectionSearchRequestInFlight: true})
      const result = collectionRequest(
        initial,
        collectionNewSearchResultsRecieved()
      )
      expect(result.collectionSearchRequestInFlight).toBeFalsy()
    })

    it('when new page completes', function(){
      const initial = Immutable({collectionSearchRequestInFlight: true})
      const result = collectionRequest(initial, collectionMoreResultsRecieved())
      expect(result.collectionSearchRequestInFlight).toBeFalsy()
    })

    it('when request errors', function(){
      const initial = Immutable({collectionSearchRequestInFlight: true})
      const result = collectionRequest(initial, collectionSearchError())
      expect(result.collectionSearchRequestInFlight).toBeFalsy()
    })
  })
})
