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

    expect(result).toEqual({
      granuleSearchRequestInFlight: false,
      errorMessage: '',
    })
  })

  it('new search marks inFlight', function(){
    const initial = Immutable({granuleSearchRequestInFlight: false})
    const result = granuleRequest(initial, granuleNewSearchRequested())
    expect(result.granuleSearchRequestInFlight).toBeTruthy()
  })

  it('next page marks inFlight', function(){
    const initial = Immutable({granuleSearchRequestInFlight: false})
    const result = granuleRequest(initial, granuleMoreResultsRequested())
    expect(result.granuleSearchRequestInFlight).toBeTruthy()
  })

  it('new search resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous search request',
    })
    const result = granuleRequest(initial, granuleNewSearchRequested())
    expect(result.errorMessage).toBe('')
  })

  it('next page resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous search request',
    })
    const result = granuleRequest(initial, granuleMoreResultsRequested())
    expect(result.errorMessage).toBe('')
  })

  it('result from search resets inFlight', function(){
    const initial = Immutable({granuleSearchRequestInFlight: true})
    const result = granuleRequest(initial, granuleNewSearchResultsRecieved())
    expect(result.granuleSearchRequestInFlight).toBeFalsy()
  })

  it('result from next page resets inFlight', function(){
    const initial = Immutable({granuleSearchRequestInFlight: true})
    const result = granuleRequest(initial, granuleMoreResultsRecieved())
    expect(result.granuleSearchRequestInFlight).toBeFalsy()
  })

  it('error resets inFlight, provides errorMessage', function(){
    const initial = Immutable({granuleSearchRequestInFlight: true})
    const result = granuleRequest(
      initial,
      granuleSearchError('Cause of Error: test case')
    )
    expect(result.granuleSearchRequestInFlight).toBeFalsy()
    expect(result.errorMessage).toBe('Cause of Error: test case')
  })
})
