import Immutable from 'seamless-immutable'
import {
  collectionRequest,
  initialState,
} from '../../../src/reducers/search/collectionRequest'
import {
  collectionNewSearchRequested,
  collectionNewSearchResetFiltersRequested,
  collectionResultsPageRequested,
  collectionNewSearchResultsReceived,
  collectionResultsPageReceived,
  collectionSearchError,
} from '../../../src/actions/routing/CollectionSearchStateActions'

describe('The collection request reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionRequest(initialState, initialAction)

    expect(result).toEqual({
      inFlight: false,
      errorMessage: '',
    })
  })

  it('new search marks inFlight', function(){
    const initial = Immutable({inFlight: false})
    const result = collectionRequest(initial, collectionNewSearchRequested())
    expect(result.inFlight).toBeTruthy()
  })

  it('new search marks inFlight, resets error message', function(){
    const initial = Immutable({inFlight: false})
    const result = collectionRequest(
      initial,
      collectionNewSearchResetFiltersRequested({})
    )
    expect(result.inFlight).toBeTruthy()
    expect(result.errorMessage).toBe('')
  })

  it('next page marks inFlight', function(){
    const initial = Immutable({inFlight: false})
    const result = collectionRequest(initial, collectionResultsPageRequested())
    expect(result.inFlight).toBeTruthy()
  })

  it('new search resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous search request',
    })
    const result = collectionRequest(initial, collectionNewSearchRequested())
    expect(result.errorMessage).toBe('')
  })

  it('next page resets errorMessage', function(){
    const initial = Immutable({
      errorMessage: 'error from previous search request',
    })
    const result = collectionRequest(initial, collectionResultsPageRequested())
    expect(result.errorMessage).toBe('')
  })

  it('result from search resets inFlight', function(){
    const initial = Immutable({inFlight: true})
    const result = collectionRequest(
      initial,
      collectionNewSearchResultsReceived()
    )
    expect(result.inFlight).toBeFalsy()
  })

  it('result from next page resets inFlight', function(){
    const initial = Immutable({inFlight: true})
    const result = collectionRequest(
      initial,
      collectionResultsPageReceived(0, [])
    )
    expect(result.inFlight).toBeFalsy()
  })

  it('error resets inFlight, provides errorMessage', function(){
    const initial = Immutable({inFlight: true})
    const result = collectionRequest(
      initial,
      collectionSearchError('Cause of Error: test case')
    )
    expect(result.inFlight).toBeFalsy()
    expect(result.errorMessage).toBe('Cause of Error: test case')
  })
})
