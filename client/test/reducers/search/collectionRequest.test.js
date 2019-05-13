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

    expect(result).toEqual({
      collectionSearchRequestInFlight: false,
      errorMessage: '',
    })
  })

  it('new search marks inFlight', function(){
    const initial = Immutable({collectionSearchRequestInFlight: false})
    const result = collectionRequest(initial, collectionNewSearchRequested())
    expect(result.collectionSearchRequestInFlight).toBeTruthy()
  })

  it('next page marks inFlight', function(){
    const initial = Immutable({collectionSearchRequestInFlight: false})
    const result = collectionRequest(initial, collectionMoreResultsRequested())
    expect(result.collectionSearchRequestInFlight).toBeTruthy()
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
    const result = collectionRequest(initial, collectionMoreResultsRequested())
    expect(result.errorMessage).toBe('')
  })

  it('result from search resets inFlight', function(){
    const initial = Immutable({collectionSearchRequestInFlight: true})
    const result = collectionRequest(
      initial,
      collectionNewSearchResultsRecieved()
    )
    expect(result.collectionSearchRequestInFlight).toBeFalsy()
  })

  it('result from next page resets inFlight', function(){
    const initial = Immutable({collectionSearchRequestInFlight: true})
    const result = collectionRequest(initial, collectionMoreResultsRecieved())
    expect(result.collectionSearchRequestInFlight).toBeFalsy()
  })

  it('error resets inFlight, provides errorMessage', function(){
    const initial = Immutable({collectionSearchRequestInFlight: true})
    const result = collectionRequest(
      initial,
      collectionSearchError('Cause of Error: test case')
    )
    expect(result.collectionSearchRequestInFlight).toBeFalsy()
    expect(result.errorMessage).toBe('Cause of Error: test case')
  })
})
