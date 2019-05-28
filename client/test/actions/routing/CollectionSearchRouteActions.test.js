import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {
  submitCollectionSearch,
  submitCollectionSearchWithFilter,
  submitCollectionSearchWithQueryText,
  submitCollectionSearchNextPage,
} from '../../../src/actions/routing/CollectionSearchRouteActions'
import {
  // used to set up pre-test conditions
  collectionNewSearchRequested, // TODO these might be easier to clean up a little the resetFilters version
  collectionNewSearchResultsReceived,
  collectionUpdateQueryText,
  collectionUpdateDateRange,
} from '../../../src/actions/routing/CollectionSearchStateActions'

let history_input = {}

const mockHistoryPush = input => {
  history_input = input
}
const mockHistory = {
  push: mockHistoryPush,
  location: {pathname: 'test', search: null},
}

describe('collection search actions', function(){
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
  const mockNextPagePayload = {
    data: [
      {
        id: 'uuid-XYZ',
        attributes: {
          title: 'XYZ',
        },
      },
      {
        id: 'uuid-987',
        attributes: {
          title: '987',
        },
      },
    ],
    meta: {
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

  describe('new search with query', function(){
    it('resets the previous filters', function(){
      // set up a time filter in addition to the queryText
      store.dispatch(collectionUpdateDateRange('2017', '2018'))
      expect(store.getState().search.collectionFilter.startDateTime).toEqual(
        '2017'
      )
      expect(store.getState().search.collectionFilter.endDateTime).toEqual(
        '2018'
      )
      expect(store.getState().search.collectionFilter.queryText).toEqual('demo')

      // then start a request
      store.dispatch(submitCollectionSearchWithQueryText(mockHistory, 'hello'))
      const collectionFilter = store.getState().search.collectionFilter
      expect(collectionFilter.startDateTime).toBeNull()
      expect(collectionFilter.endDateTime).toBeNull()
      expect(collectionFilter.queryText).toEqual('hello')
      expect(history_input).toEqual({
        pathname: '/collections',
        search: '?q=hello',
      })
    })
  })

  describe('new search with filters', function(){
    it('resets the previous filters', function(){
      // set up a time filter in addition to the queryText
      store.dispatch(collectionUpdateDateRange('2017', '2018'))
      expect(store.getState().search.collectionFilter.startDateTime).toEqual(
        '2017'
      )
      expect(store.getState().search.collectionFilter.endDateTime).toEqual(
        '2018'
      )
      expect(store.getState().search.collectionFilter.queryText).toEqual('demo')

      // then start a request
      store.dispatch(
        submitCollectionSearchWithFilter(mockHistory, {startDateTime: '1998'})
      )
      const collectionFilter = store.getState().search.collectionFilter
      expect(collectionFilter.startDateTime).toEqual('1998')
      expect(collectionFilter.endDateTime).toBeNull()
      expect(collectionFilter.queryText).toEqual('')
      expect(history_input).toEqual({
        pathname: '/collections',
        search: '?s=1998',
      })
    })
  })

  describe('new search', function(){
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

  describe('next page', function(){
    it('stops request if something already in flight', function(){
      //setup send something into flight first
      store.dispatch(collectionNewSearchRequested())
      expect(store.getState().search.collectionRequest.inFlight).toBeTruthy()

      // then try to start a request
      store.dispatch(submitCollectionSearchNextPage())
      expect(store.getState().search.collectionFilter.pageOffset).toBe(0)
    })

    it('executes prefetch actions', function(){
      expect(store.getState().search.collectionRequest.inFlight).toBeFalsy()
      store.dispatch(submitCollectionSearchNextPage())
      expect(history_input).toEqual({}) // it does not do history.push
      expect(store.getState().search.collectionFilter.pageOffset).toBe(20)
      expect(store.getState().search.collectionRequest.inFlight).toBeTruthy()
    })

    it('success path', async () => {
      // set up the 'first page' results
      store.dispatch(
        collectionNewSearchResultsReceived(
          10,
          [
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
          mockFacets
        )
      )

      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/collection`,
        mockNextPagePayload
      )

      await store.dispatch(submitCollectionSearchNextPage())

      const {
        collectionRequest,
        collectionResult,
        collectionFilter,
      } = store.getState().search

      expect(collectionFilter.pageOffset).toBe(20)
      expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(collectionRequest.errorMessage).toEqual('')
      expect(collectionResult.collections).toEqual({
        'uuid-ABC': {title: 'ABC'},
        'uuid-123': {title: '123'},
        'uuid-XYZ': {title: 'XYZ'},
        'uuid-987': {title: '987'},
      })
      expect(collectionResult.facets).toEqual(mockFacets)
      expect(collectionResult.totalCollectionCount).toEqual(10)
      expect(collectionResult.loadedCollectionCount).toEqual(4)
    })

    it('failure path', async () => {
      fetchMock.post((url, opts) => url == `${BASE_URL}/search/collection`, 500)

      await store.dispatch(submitCollectionSearchNextPage())
      const {collectionRequest} = store.getState().search
      expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(collectionRequest.errorMessage).toEqual(
        new Error('Internal Server Error')
      )
    })

    it('no query or filters provided is no-op', async () => {
      store.dispatch(collectionUpdateQueryText(''))
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/collection`,
        mockNextPagePayload
      )

      await store.dispatch(submitCollectionSearchNextPage())
      const {
        collectionRequest,
        collectionResult,
        collectionFilter,
      } = store.getState().search

      expect(collectionFilter.pageOffset).toBe(20) // the request was started normally
      expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(collectionRequest.errorMessage).toEqual('Invalid Request')
      expect(collectionResult.collections).toEqual({})
      expect(collectionResult.loadedCollectionCount).toEqual(0)
    })
  })
})
