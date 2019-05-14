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
} from '../../../src/actions/routing/GranuleSearchStateActions'

describe('The request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = granuleRequest(initialState, initialAction)

    expect(result).toEqual({
      inFlight: false,
      errorMessage: '',
    })
  })

  it('new search marks inFlight', function(){
    const initial = Immutable({inFlight: false})
    const result = granuleRequest(initial, granuleNewSearchRequested())
    expect(result.inFlight).toBeTruthy()
  })

  it('next page marks inFlight', function(){
    const initial = Immutable({inFlight: false})
    const result = granuleRequest(initial, granuleMoreResultsRequested())
    expect(result.inFlight).toBeTruthy()
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
    const initial = Immutable({inFlight: true})
    const result = granuleRequest(initial, granuleNewSearchResultsRecieved())
    expect(result.inFlight).toBeFalsy()
  })

  it('result from next page resets inFlight', function(){
    const initial = Immutable({inFlight: true})
    const result = granuleRequest(initial, granuleMoreResultsRecieved())
    expect(result.inFlight).toBeFalsy()
  })

  it('error resets inFlight, provides errorMessage', function(){
    const initial = Immutable({inFlight: true})
    const result = granuleRequest(
      initial,
      granuleSearchError('Cause of Error: test case')
    )
    expect(result.inFlight).toBeFalsy()
    expect(result.errorMessage).toBe('Cause of Error: test case')
  })
})
