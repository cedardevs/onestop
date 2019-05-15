import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {submitCollectionSearch} from '../../../src/actions/routing/CollectionSearchRouteActions'
import {
  collectionNewSearchRequested,
  collectionUpdateQueryText,
} from '../../../src/actions/routing/CollectionSearchStateActions'

let history_input = {}

const mockHistoryPush = input => {
  history_input = input
}
const mockHistory = {
  push: mockHistoryPush,
}

describe('collection search action', function(){
  const BASE_URL = '/-search'
  const resetStore = () => ({type: RESET_STORE})
  const mockFacets = {
    mock: 'has facets',
  }
  const mockPayload = {
    data: [
      {
        id: 'uuid-ABC',
        attributes: {
          title: 'ABC',
        },
      },
      {
        id: 'uuid-123',
        attributes: {
          title: '123',
        },
      },
    ],
    meta: {
      facets: mockFacets,
      total: 10,
    },
  }

  beforeEach(async () => {
    history_input = {}
    // reset store to initial conditions
    await store.dispatch(resetStore())
    store.dispatch(collectionUpdateQueryText('demo'))
  })

  afterEach(() => {
    fetchMock.reset()
  })

  it('stops request if something already in flight', function(){
    //setup send something into flight first
    store.dispatch(collectionNewSearchRequested())
    expect(store.getState().search.collectionRequest.inFlight).toBeTruthy()

    // then try to start a request
    store.dispatch(submitCollectionSearch(mockHistory))
    expect(history_input).toEqual({})
  })

  it('executes prefetch actions', function(){
    expect(store.getState().search.collectionRequest.inFlight).toBeFalsy()
    store.dispatch(submitCollectionSearch(mockHistory))
    expect(history_input).toEqual({
      pathname: '/collections',
      search: '?q=demo',
    })
    expect(store.getState().search.collectionRequest.inFlight).toBeTruthy()
  })

  it('success path', async () => {
    fetchMock.post(
      (url, opts) => url == `${BASE_URL}/search/collection`,
      mockPayload
    )

    await store.dispatch(submitCollectionSearch(mockHistory))
    expect(history_input).toEqual({
      pathname: '/collections',
      search: '?q=demo',
    })
    const {collectionRequest, collectionResult} = store.getState().search

    expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionRequest.errorMessage).toEqual('')
    expect(collectionResult.collections).toEqual({
      'uuid-ABC': {title: 'ABC'},
      'uuid-123': {title: '123'},
    })
    expect(collectionResult.facets).toEqual(mockFacets)
    expect(collectionResult.totalCollectionCount).toEqual(10)
    expect(collectionResult.loadedCollectionCount).toEqual(2)
  })

  it('failure path', async () => {
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/collection`, 500)

    await store.dispatch(submitCollectionSearch(mockHistory))
    const {collectionRequest, collectionResult} = store.getState().search
    expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionRequest.errorMessage).toEqual(
      new Error('Internal Server Error')
    )
    expect(collectionResult.collections).toEqual({})
    expect(collectionResult.totalCollectionCount).toEqual(0)
    expect(collectionResult.loadedCollectionCount).toEqual(0)
  })

  it('no query or filters provided is no-op', async () => {
    store.dispatch(collectionUpdateQueryText(''))
    fetchMock.post(
      (url, opts) => url == `${BASE_URL}/search/collection`,
      mockPayload
    )

    await store.dispatch(submitCollectionSearch(mockHistory))
    expect(history_input).toEqual({}) // The history push step skips things when no query/filter provided (as it should)
    const {collectionRequest, collectionResult} = store.getState().search

    expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionRequest.errorMessage).toEqual('Invalid Request')
  })
})
