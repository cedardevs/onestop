import Immutable from 'seamless-immutable'
import {
  granuleRequest,
  initialState,
} from '../../../src/reducers/search/granuleRequest'
import {
  granuleNewSearchRequested,
  granuleNewSearchResetFiltersRequested,
  granuleMoreResultsRequested,
  granuleNewSearchResultsReceived,
  granuleMoreResultsReceived,
  granuleSearchError,
} from '../../../src/actions/routing/GranuleSearchStateActions'

describe('The request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = granuleRequest(initialState, initialAction)

    expect(result).toEqual({
      inFlight: false,
      cartGranulesInFlight: false,
      errorMessage: '',
      cartGranulesErrorMessage: '',
    })
  })

  it('new search marks inFlight', function(){
    const initial = Immutable({inFlight: false})
    const result = granuleRequest(initial, granuleNewSearchRequested()) // TODO I feel like I need some verification or explanation on how this is not intended to be called without a uuid...
    expect(result.inFlight).toBeTruthy()
  })

  it('new search marks inFlight (reset filters version), plus resets error message', function(){
    const initial = Immutable({inFlight: false})
    const result = granuleRequest(
      initial,
      granuleNewSearchResetFiltersRequested()
    ) // TODO I feel like I need some verification or explanation on how this is not intended to be called without a uuid...
    expect(result.inFlight).toBeTruthy()
    expect(result.errorMessage).toEqual('')
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
    expect(result.errorMessage).toEqual('')
  })

  it('next page resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous search request',
    })
    const result = granuleRequest(initial, granuleMoreResultsRequested())
    expect(result.errorMessage).toEqual('')
  })

  it('result from search resets inFlight', function(){
    const initial = Immutable({inFlight: true})
    const result = granuleRequest(initial, granuleNewSearchResultsReceived())
    expect(result.inFlight).toBeFalsy()
  })

  it('result from next page resets inFlight', function(){
    const initial = Immutable({inFlight: true})
    const result = granuleRequest(initial, granuleMoreResultsReceived())
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
