import Immutable from 'seamless-immutable'
import {
  granuleRequest,
  initialState,
} from '../../../src/reducers/search/granuleRequest'
import {
  granuleNewSearchRequested,
  granuleMoreResultsRequested,
  granuleNewSearchResultsRecieved,
  granuleMoreResultsRecieved,
  granuleSearchError,
} from '../../../src/actions/search/GranuleRequestActions'

describe('The request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = granuleRequest(initialState, initialAction)

    expect(result).toEqual({granuleSearchRequestInFlight: false})
  })

  describe('updates in-flight value', function(){
    it('when starting a new search', function(){
      const initial = Immutable({granuleSearchRequestInFlight: false})
      const result = granuleRequest(initial, granuleNewSearchRequested())
      expect(result.granuleSearchRequestInFlight).toBeTruthy()
    })
    it('when starting a new page request', function(){
      const initial = Immutable({granuleSearchRequestInFlight: false})
      const result = granuleRequest(initial, granuleMoreResultsRequested())
      expect(result.granuleSearchRequestInFlight).toBeTruthy()
    })

    it('when new search completes', function(){
      const initial = Immutable({granuleSearchRequestInFlight: true})
      const result = granuleRequest(initial, granuleNewSearchResultsRecieved())
      expect(result.granuleSearchRequestInFlight).toBeFalsy()
    })

    it('when new page completes', function(){
      const initial = Immutable({granuleSearchRequestInFlight: true})
      const result = granuleRequest(initial, granuleMoreResultsRecieved())
      expect(result.granuleSearchRequestInFlight).toBeFalsy()
    })

    it('when request errors', function(){
      const initial = Immutable({granuleSearchRequestInFlight: true})
      const result = granuleRequest(initial, granuleSearchError())
      expect(result.granuleSearchRequestInFlight).toBeFalsy()
    })
  })
})
