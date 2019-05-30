import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {
  submitCollectionDetail,
  submitCollectionDetailAndUpdateUrl,
} from '../../../src/actions/routing/CollectionDetailRouteActions'
import {
  collectionDetailRequested,
  granuleMatchingCountRequested,
} from '../../../src/actions/routing/CollectionDetailStateActions'

let history_input = {}
let historyPushCallCount = 0

const mockHistoryPush = input => {
  history_input = input
  historyPushCallCount = historyPushCallCount + 1
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
    historyPushCallCount = 0
    mockHistory.location = {pathname: 'test', search: null}
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

  describe('when a detail request is already in flight', function(){
    beforeEach(async () => {
      store.dispatch(collectionDetailRequested('uuid-XYZ'))
    })
    it('submit (+url) does not continue with a submit request', function(){
      store.dispatch(
        submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {})
      )
      expect(
        store.getState().search.collectionDetailRequest.requestedID
      ).toEqual('uuid-XYZ') // this would be uuid-ABC if the request went through
    })
    it('submit does not continue with a submit request', function(){
      store.dispatch(submitCollectionDetail('uuid-ABC', {}))
      expect(
        store.getState().search.collectionDetailRequest.requestedID
      ).toEqual('uuid-XYZ') // this would be uuid-ABC if the request went through
    })
  })

  describe('when a granule count request is already in flight', function(){
    beforeEach(async () => {
      store.dispatch(granuleMatchingCountRequested())
    })
    it('submit (+url) does not continue with a submit request', function(){
      store.dispatch(
        submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {})
      )

      const {collectionDetailRequest} = store.getState().search
      expect(collectionDetailRequest.inFlight).toBeFalsy()
      expect(collectionDetailRequest.requestedID).toEqual(null) // this would be uuid-ABC if the request went through
    })
    it('submit does not continue with a submit request', function(){
      store.dispatch(submitCollectionDetail('uuid-ABC', {}))

      const {collectionDetailRequest} = store.getState().search
      expect(collectionDetailRequest.inFlight).toBeFalsy()
      expect(collectionDetailRequest.requestedID).toEqual(null) // this would be uuid-ABC if the request went through
    })
  })

  describe('when id is null', function(){
    beforeEach(async () => {
      store.dispatch(granuleMatchingCountRequested())
    })
    it('submit (+url) does not continue with a submit request', function(){
      store.dispatch(submitCollectionDetailAndUpdateUrl(mockHistory, null, {}))

      const {collectionDetailRequest} = store.getState().search
      expect(collectionDetailRequest.inFlight).toBeFalsy()
    })
    it('submit does not continue with a submit request', function(){
      store.dispatch(submitCollectionDetail(null, {}))

      const {collectionDetailRequest} = store.getState().search
      expect(collectionDetailRequest.inFlight).toBeFalsy()
    })
  })

  describe('when id is empty string', function(){
    beforeEach(async () => {
      store.dispatch(granuleMatchingCountRequested())
    })
    it('submit (+url) does not continue with a submit request', function(){
      store.dispatch(submitCollectionDetailAndUpdateUrl(mockHistory, '', {}))

      const {collectionDetailRequest} = store.getState().search
      expect(collectionDetailRequest.inFlight).toBeFalsy()
    })
    it('submit does not continue with a submit request', function(){
      store.dispatch(submitCollectionDetail('', {}))

      const {collectionDetailRequest} = store.getState().search
      expect(collectionDetailRequest.inFlight).toBeFalsy()
    })
  })

  it('executes prefetch actions and updates the URL', function(){
    expect(store.getState().search.collectionDetailRequest.inFlight).toBeFalsy()

    store.dispatch(
      submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {})
    )
    const {
      collectionDetailRequest,
      collectionDetailFilter,
    } = store.getState().search
    expect(collectionDetailRequest.inFlight).toBeTruthy()
    expect(collectionDetailRequest.backgroundInFlight).toBeTruthy()
    expect(collectionDetailRequest.requestedID).toEqual('uuid-ABC')
    expect(collectionDetailFilter.selectedIds).toEqual([ 'uuid-ABC' ])

    expect(history_input).toEqual({
      pathname: '/collections/details/uuid-ABC',
      search: null,
    })
    // TODO worth asserting other filter state, or let it be assumed by the tests directly against that reducer + action?
  })

  it('executes prefetch actions without updating the URL', function(){
    expect(store.getState().search.collectionDetailRequest.inFlight).toBeFalsy()

    store.dispatch(submitCollectionDetail('uuid-ABC', {}))
    const {
      collectionDetailRequest,
      collectionDetailFilter,
    } = store.getState().search
    expect(collectionDetailRequest.inFlight).toBeTruthy()
    expect(collectionDetailRequest.backgroundInFlight).toBeTruthy()
    expect(collectionDetailRequest.requestedID).toEqual('uuid-ABC')
    expect(collectionDetailFilter.selectedIds).toEqual([ 'uuid-ABC' ])

    expect(historyPushCallCount).toEqual(0)

    // TODO worth asserting other filter state, or let it be assumed by the tests directly against that reducer + action?
  })

  it('does not push to history if already there', function(){
    mockHistory.location = {
      pathname: '/collections/details/uuid-ABC',
      search: '?s=2017-01-01T00%3A00%3A00Z',
    }

    store.dispatch(
      submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {
        startDateTime: '2017-01-01T00:00:00Z',
      })
    )
    const {
      collectionDetailRequest,
      collectionDetailFilter,
    } = store.getState().search

    expect(historyPushCallCount).toEqual(0)
  })

  it('success path with URL change', async () => {
    // TODO refactor these tests to have shared assert blocks (where appropriate?)
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
      meta: {
        total: 10,
      },
    })

    await store.dispatch(
      submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {
        startDateTime: '2017-01-01T00:00:00Z',
      })
    )

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

    expect(historyPushCallCount).toEqual(1)
    expect(history_input).toEqual({
      pathname: '/collections/details/uuid-ABC',
      search: '?s=2017-01-01T00%3A00%3A00Z',
    })
  })

  it('success path', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
      meta: {
        total: 10,
      },
    })

    await store.dispatch(
      submitCollectionDetail('uuid-ABC', {
        startDateTime: '2017-01-01T00:00:00Z',
      })
    )

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

    expect(historyPushCallCount).toEqual(0)
  })

  it('failure path (url)', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, 404)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
      meta: {
        total: 10,
      },
    })
    await store.dispatch(
      submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {})
    )
    const {
      collectionDetailRequest,
      collectionDetailResult,
    } = store.getState().search
    expect(collectionDetailRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionDetailRequest.errorMessage).toEqual(new Error('Not Found'))
    expect(collectionDetailResult.collection).toBeNull()
    expect(collectionDetailRequest.backgroundInFlight).toBeFalsy() // after completing the request, this should be reset too

    expect(historyPushCallCount).toEqual(1)
  })

  it('failure path', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, 404)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
      meta: {
        total: 10,
      },
    })
    await store.dispatch(submitCollectionDetail('uuid-ABC', {}))
    const {
      collectionDetailRequest,
      collectionDetailResult,
    } = store.getState().search
    expect(collectionDetailRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    expect(collectionDetailRequest.errorMessage).toEqual(new Error('Not Found'))
    expect(collectionDetailResult.collection).toBeNull()
    expect(collectionDetailRequest.backgroundInFlight).toBeFalsy() // after completing the request, this should be reset too

    expect(historyPushCallCount).toEqual(0)
  })

  it('failure background request (url)', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 404)
    await store.dispatch(
      submitCollectionDetailAndUpdateUrl(mockHistory, 'uuid-ABC', {})
    )
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
    expect(historyPushCallCount).toEqual(1)
  })
  it('failure background request', async () => {
    fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
    fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 404)
    await store.dispatch(submitCollectionDetail('uuid-ABC', {}))
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
    expect(historyPushCallCount).toEqual(0)
  })
})
