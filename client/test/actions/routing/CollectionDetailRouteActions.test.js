import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {submitCollectionDetail} from '../../../src/actions/routing/CollectionDetailRouteActions'
import {collectionDetailRequested} from '../../../src/actions/routing/CollectionDetailStateActions'

let history_input = {}

const mockHistoryPush = input => {
  history_input = input
}
const mockHistory = {
  push: mockHistoryPush,
  location: {pathname: 'test', search: null},
}

describe('collection detail action', function(){
  const BASE_URL = '/-search'
  const resetStore = () => ({type: RESET_STORE})
  beforeEach(async () => {
    history_input = {}
    // reset store to initial conditions
    await store.dispatch(resetStore())
  })

  afterEach(() => {
    fetchMock.reset()
  })

  const mockCollection = {
    id: 'uuid-ABC',
    mockedResponse: 'yes',
  }
  const mockPayload = {
    data: [ mockCollection ],
    meta: {
      totalGranules: 13,
    },
  }

  it('stops request if something already in flight', function(){
    //setup send something into flight first
    store.dispatch(collectionDetailRequested('uuid-XYZ'))
    expect(
      store.getState().search.collectionDetailRequest.inFlight
    ).toBeTruthy()

    // then try to start a request
    store.dispatch(submitCollectionDetail(mockHistory, 'uuid-ABC', {}))
    expect(history_input).toEqual({})
  })

  it('executes prefetch actions', function(){
    expect(store.getState().search.collectionDetailRequest.inFlight).toBeFalsy()
    store.dispatch(submitCollectionDetail(mockHistory, 'uuid-ABC', {}))
    expect(history_input).toEqual({
      pathname: '/collections/details/uuid-ABC',
      search: null,
    })
    expect(
      store.getState().search.collectionDetailRequest.inFlight
    ).toBeTruthy()
    expect(store.getState().search.collectionDetailRequest.requestedID).toBe(
      'uuid-ABC'
    )
  })

  it('success path', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
      meta: {
        total: 10,
      },
    })

    await store.dispatch(
      submitCollectionDetail(mockHistory, 'uuid-ABC', {
        startDateTime: '2017-01-01T00:00:00Z',
      })
    )
    expect(history_input).toEqual({
      pathname: '/collections/details/uuid-ABC',
      search: '?s=2017-01-01T00%3A00%3A00Z',
    })
    const {
      collectionDetailRequest,
      collectionDetailResult,
    } = store.getState().search

    expect(collectionDetailRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionDetailRequest.errorMessage).toEqual('')
    expect(collectionDetailResult.collection).toEqual(mockCollection)
    expect(collectionDetailResult.totalGranuleCount).toEqual(13)
    expect(collectionDetailRequest.backgroundInFlight).toBeFalsy()
    expect(collectionDetailRequest.backgroundErrorMessage).toEqual('')
    expect(collectionDetailResult.filteredGranuleCount).toEqual(10)
  })

  it('failure path', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, 404)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
      meta: {
        total: 10,
      },
    })
    await store.dispatch(submitCollectionDetail(mockHistory, 'uuid-ABC', {}))
    const {
      collectionDetailRequest,
      collectionDetailResult,
    } = store.getState().search
    expect(collectionDetailRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionDetailRequest.errorMessage).toEqual(new Error('Not Found'))
    expect(collectionDetailResult.collection).toBeNull()
  })

  it('failured background request', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 404)
    await store.dispatch(submitCollectionDetail(mockHistory, 'uuid-ABC', {}))
    const {
      collectionDetailRequest,
      collectionDetailResult,
    } = store.getState().search
    expect(collectionDetailRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionDetailRequest.errorMessage).toEqual('')
    expect(collectionDetailResult.collection).toEqual(mockCollection)
    expect(collectionDetailResult.totalGranuleCount).toEqual(13)
    expect(collectionDetailRequest.backgroundInFlight).toBeFalsy()
    expect(collectionDetailResult.filteredGranuleCount).toEqual(0)
    expect(collectionDetailRequest.backgroundErrorMessage).toEqual(
      new Error('Not Found')
    )
  })

  it('cannot handle invalid params', function(){
    const action = () => {
      store.dispatch(submitCollectionDetail(mockHistory))
    }
    expect(action).toThrow(TypeError)
  })
})
