import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'
import * as spyableActions from '../../../src/actions/routing/CollectionSearchStateActions'
import * as spyOnQueryUtils from '../../../src/utils/queryUtils'

import {
  submitCollectionSearch,
  submitCollectionSearchWithFilter,
  submitCollectionSearchWithQueryText,
  submitCollectionSearchWithPage,
} from '../../../src/actions/routing/CollectionSearchRouteActions'
import {
  // used to set up pre-test conditions
  collectionNewSearchResetFiltersRequested,
  collectionResultsPageRequested,
  collectionNewSearchResultsReceived,
  collectionResultsPageReceived,
  collectionUpdateDateRange,
} from '../../../src/actions/routing/CollectionSearchStateActions'

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

describe('collection search actions', function(){
  const BASE_URL = '/onestop/api/search'
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
    historyPushCallCount = 0
    mockHistory.location = {pathname: 'test', search: null}
    // reset store to initial conditions
    await store.dispatch(resetStore())

    // // set default queryText
    // await store.dispatch(submitCollectionSearchWithQueryText('demo'))
    // const {collectionFilter} = store.getState().search
    // expect(collectionFilter.queryText).toEqual('demo')
  })

  afterEach(() => {
    fetchMock.reset()
  })

  const submitSearchCase = {
    name: 'submit new search',
    function: submitCollectionSearch,
    params: [ mockHistory ],
    expectedURL: {
      pathname: '/collections',
      search: '?q=demo',
    },
  }
  const submitSearchWithQueryTextCase = {
    name: 'submit new search with query text',
    function: submitCollectionSearchWithQueryText,
    params: [ mockHistory, 'hello' ],
    expectedURL: {
      pathname: '/collections',
      search: '?q=hello',
    },
  }
  const submitSearchWithFilterCase = {
    name: 'submit new search with filters',
    function: submitCollectionSearchWithFilter,
    params: [ mockHistory, {startDateTime: '1998'} ],
    expectedURL: {
      pathname: '/collections',
      search: '?tr=i&s=1998', // default intersects relation
    },
  }
  const submitNextPageCase = {
    name: 'submit next page',
    function: submitCollectionSearchWithPage,
    params: [ 2, 2 ],
  }

  const standardNewSearchTestCases = [
    submitSearchCase,
    submitSearchWithQueryTextCase,
    submitSearchWithFilterCase,
  ]

  const allTestCases = [
    submitSearchCase,
    submitSearchWithQueryTextCase,
    submitSearchWithFilterCase,
    submitNextPageCase,
  ]

  describe('with demo querytext', function(){
    beforeEach(async () => {
      // set default queryText
      // await store.dispatch(submitCollectionSearchWithQueryText('demo'))
      store.dispatch(
        collectionNewSearchResetFiltersRequested({queryText: 'demo'})
      )
      store.dispatch(collectionNewSearchResultsReceived(0, [], []))

      const {collectionFilter, collectionRequest} = store.getState().search
      expect(collectionFilter.queryText).toEqual('demo')
      expect(collectionRequest.inFlight).toBeFalsy()
    })

    describe('new searches treat filters differently', function(){
      beforeEach(async () => {
        // set up existing filters with lazily formatted dates
        store.dispatch(collectionUpdateDateRange('2017', '2018'))
        const {collectionFilter} = store.getState().search
        expect(collectionFilter.startDateTime).toEqual('2017')
        expect(collectionFilter.endDateTime).toEqual('2018')
        expect(collectionFilter.queryText).toEqual('demo')
      })

      it(`${submitSearchCase.name} does not change existing filters`, function(){
        store.dispatch(submitSearchCase.function(...submitSearchCase.params))

        const collectionFilter = store.getState().search.collectionFilter
        expect(collectionFilter.startDateTime).toEqual('2017')
        expect(collectionFilter.endDateTime).toEqual('2018')
        expect(collectionFilter.queryText).toEqual('demo')
      })

      it(`${submitSearchWithQueryTextCase.name} resets existing filters`, function(){
        store.dispatch(
          submitSearchWithQueryTextCase.function(
            ...submitSearchWithQueryTextCase.params
          )
        )

        const collectionFilter = store.getState().search.collectionFilter
        expect(collectionFilter.startDateTime).toBeNull()
        expect(collectionFilter.endDateTime).toBeNull()
        expect(collectionFilter.queryText).toEqual('hello')
      })

      it(`${submitSearchWithFilterCase.name} resets existing filters`, function(){
        store.dispatch(
          submitSearchWithFilterCase.function(
            ...submitSearchWithFilterCase.params
          )
        )

        const collectionFilter = store.getState().search.collectionFilter
        expect(collectionFilter.startDateTime).toEqual('1998')
        expect(collectionFilter.endDateTime).toBeNull()
        expect(collectionFilter.queryText).toEqual('')
      })
    })

    describe('submit overwrites inFlight', function(){
      beforeEach(async () => {
        //setup send something into flight first
        store.dispatch(collectionResultsPageRequested(20, 20))
        const {collectionRequest, collectionFilter} = store.getState().search
        expect(collectionRequest.inFlight).toBeTruthy()
        expect(collectionFilter.pageOffset).toBe(20)
      })

      it(`${submitNextPageCase.name} replaces in flight request`, function(){
        store.dispatch(
          submitNextPageCase.function(...submitNextPageCase.params)
        )

        expect(historyPushCallCount).toEqual(0) // all new searches push a new history request right now (although next page would not)
        expect(store.getState().search.collectionFilter.pageOffset).toBe(2)
        expect(store.getState().search.collectionFilter.pageSize).toBe(2) // but just in case, this definitely should always be reset to 0, or changed to 40
      })

      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name} replaces in flight request`, function(){
          store.dispatch(testCase.function(...testCase.params))

          expect(historyPushCallCount).toEqual(1) // all new searches push a new history request right now (although next page would not)
          expect(store.getState().search.collectionFilter.pageOffset).toBe(0) // but just in case, this definitely should always be reset to 0, or changed to 40
        })
      })
    })

    describe('prefetch actions', function(){
      beforeEach(async () => {
        // pretend next page has been triggered and completed, so that pageOffset has been modified by a prior search
        store.dispatch(collectionResultsPageRequested(20, 20))
        store.dispatch(collectionNewSearchResultsReceived(0, [], {}))
        const {collectionRequest, collectionFilter} = store.getState().search
        expect(collectionRequest.inFlight).toBeFalsy()
        expect(collectionFilter.pageOffset).toEqual(20)
        expect(collectionFilter.pageSize).toEqual(20)
      })

      describe('all submit options update the state correctly', function(){
        standardNewSearchTestCases.forEach(function(testCase){
          it(`${testCase.name}`, function(){
            store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionFilter,
            } = store.getState().search

            expect(collectionRequest.inFlight).toBeTruthy()
            expect(collectionFilter.pageOffset).toEqual(0)
          })
        })

        it(`${submitNextPageCase.name}`, function(){
          store.dispatch(
            submitNextPageCase.function(...submitNextPageCase.params)
          )

          const {collectionRequest, collectionFilter} = store.getState().search

          expect(collectionRequest.inFlight).toBeTruthy()
          expect(collectionFilter.pageOffset).toEqual(2)
        })
      })

      describe('each submit function treats the URL differently', function(){
        standardNewSearchTestCases.forEach(function(testCase){
          it(`${testCase.name} updates the URL`, function(){
            store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionFilter,
            } = store.getState().search

            expect(historyPushCallCount).toEqual(1)
            expect(history_input).toEqual(testCase.expectedURL)
          })
        })

        standardNewSearchTestCases.forEach(function(testCase){
          it(`${testCase.name} does not update history if no change`, function(){
            mockHistory.location = testCase.expectedURL
            store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionFilter,
            } = store.getState().search

            expect(historyPushCallCount).toEqual(0)
          })
        })

        it(`${submitNextPageCase.name} does not update the URL`, function(){
          store.dispatch(
            submitNextPageCase.function(...submitNextPageCase.params)
          )

          expect(historyPushCallCount).toEqual(0)
        })
      })
    })

    describe('interrupt in flight request', () => {
      const assembleSearchRequest = jest.spyOn(
        spyOnQueryUtils,
        'assembleSearchRequest'
      )
      const collectionResultsPageReceived = jest.spyOn(
        spyableActions,
        'collectionResultsPageReceived'
      )
      const collectionNewSearchResultsReceived = jest.spyOn(
        spyableActions,
        'collectionNewSearchResultsReceived'
      )
      const collectionSearchError = jest.spyOn(
        spyableActions,
        'collectionSearchError'
      )
      beforeEach(async () => {
        assembleSearchRequest.mockClear()
        collectionNewSearchResultsReceived.mockClear()
        collectionResultsPageReceived.mockClear()
        collectionSearchError.mockClear()
      })
      afterAll(async () => {
        assembleSearchRequest.mockClear()
        collectionNewSearchResultsReceived.mockClear()
        collectionResultsPageReceived.mockClear()
        collectionSearchError.mockClear()
        // restore the original (non-mocked) implementation:
        assembleSearchRequest.mockRestore()
        collectionNewSearchResultsReceived.mockRestore()
        collectionResultsPageReceived.mockRestore()
        collectionSearchError.mockRestore()
      })

      test('new filter interrupted by next page leaves the UI (redux store) in consistent state', async () => {
        fetchMock
          // mock the results of the first search request:
          .postOnce((url, opts) => url == `${BASE_URL}/search/collection`, {
            data: [
              {
                id: 'INTERRUPTED',
              },
            ],
            meta: {
              facets: {},
              total: 2,
            },
          })
          // mock the results of the second search request:
          .postOnce(
            (url, opts) => url == `${BASE_URL}/search/collection`,
            mockPayload
          )
        //setup send something into flight first

        // manually trigger the first layer of the dispatch (preflight actions), while storing the promise to resolve later. This mimics a long-running initial query that will be interrupted by a subsequent query
        // initially we trigger a query with a filter
        const promise = submitSearchWithFilterCase.function(
          ...submitSearchWithFilterCase.params
        )(store.dispatch, store.getState)

        {
          // keep scoped
          const {collectionRequest} = store.getState().search
          expect(collectionRequest.inFlight).toBeTruthy()
        }

        // which is interrupted by a Next Page query
        await store.dispatch(
          submitNextPageCase.function(...submitNextPageCase.params)
        )

        // allow the initial request to resolve
        await promise

        const {collectionRequest, collectionResult} = store.getState().search

        // we made 2 fetches
        expect(fetchMock.calls().length).toEqual(2)
        expect(assembleSearchRequest.mock.calls.length).toEqual(2)
        expect(collectionNewSearchResultsReceived.mock.calls.length).toEqual(0) // first request never completed and called this!
        expect(collectionResultsPageReceived.mock.calls.length).toEqual(1)

        expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
        expect(collectionRequest.errorMessage).toEqual('')
        expect(collectionResult.collections).toEqual({
          'uuid-ABC': {title: 'ABC'},
          'uuid-123': {title: '123'},
        })

        const finalStateToAssembleQueryFrom =
          assembleSearchRequest.mock.calls[1][0]
        expect(finalStateToAssembleQueryFrom.startDateTime).toEqual('1998') // has filter from the first request
        expect(finalStateToAssembleQueryFrom.pageOffset).toEqual(2) // has page offset from the second request
        expect(finalStateToAssembleQueryFrom.pageSize).toEqual(2)
        expect(assembleSearchRequest.mock.results[1].value).toEqual({
          // confirm result of assemble query has both filter and page pieces
          facets: false,
          filters: [
            {
              after: '1998',
              relation: 'intersects',
              type: 'datetime',
            },
          ],
          page: {
            max: 2,
            offset: 2,
          },
          queries: [],
        }) // create the request with both the recently applied filter (for the request that did not complete) PLUS the next page offset
        expect(
          collectionResultsPageReceived.mock.results[0].value.items
        ).toEqual(mockPayload.data) // and not "INTERRUPTED" payload
      })

      test('new search request which errors', async () => {
        fetchMock
          // mock the results of the first search request:
          .postOnce((url, opts) => url == `${BASE_URL}/search/collection`, 400)
          // mock the results of the second search request:
          .postOnce((url, opts) => url == `${BASE_URL}/search/collection`, 500)
        //setup send something into flight first

        // manually trigger the first layer of the dispatch (preflight actions), while storing the promise to resolve later. This mimics a long-running initial query that will be interrupted by a subsequent query
        const promise = submitSearchWithQueryTextCase.function(
          ...submitSearchWithQueryTextCase.params
        )(store.dispatch, store.getState)

        {
          // keep scoped
          const {collectionRequest} = store.getState().search
          expect(collectionRequest.inFlight).toBeTruthy()
        }

        await store.dispatch(
          submitSearchWithFilterCase.function(
            ...submitSearchWithFilterCase.params
          )
        )

        // allow the initial request to resolve
        await promise

        const {collectionRequest, collectionResult} = store.getState().search

        // we made 2 fetches
        expect(fetchMock.calls().length).toEqual(2)
        // but only one collectionSearchError via errorHandler
        expect(collectionSearchError.mock.calls.length).toEqual(1)

        expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
        expect(collectionRequest.errorMessage).toEqual(
          new Error('Internal Server Error')
        )
        expect(collectionResult.collections).toEqual({})
      })

      test('new search request success', async () => {
        fetchMock
          // mock the results of the first search request:
          .postOnce((url, opts) => url == `${BASE_URL}/search/collection`, {
            data: [
              {
                id: 'INTERRUPTED',
              },
            ],
            meta: {
              facets: {},
              total: 2,
            },
          })
          // mock the results of the second search request:
          .postOnce(
            (url, opts) => url == `${BASE_URL}/search/collection`,
            mockPayload
          )
        //setup send something into flight first

        // manually trigger the first layer of the dispatch (preflight actions), while storing the promise to resolve later. This mimics a long-running initial query that will be interrupted by a subsequent query
        const promise = submitSearchWithQueryTextCase.function(
          ...submitSearchWithQueryTextCase.params
        )(store.dispatch, store.getState)

        {
          // keep scoped
          const {collectionRequest} = store.getState().search
          expect(collectionRequest.inFlight).toBeTruthy()
        }

        await store.dispatch(
          submitSearchWithFilterCase.function(
            ...submitSearchWithFilterCase.params
          )
        )

        // allow the initial request to resolve (red-green testing: without the fetch checking the abort controller status, this would cause the number of calls to collectionNewSearchResultsReceived to be incorrect)
        await promise

        const {collectionRequest, collectionResult} = store.getState().search

        // we made 2 fetches
        expect(fetchMock.calls().length).toEqual(2)
        // but only one collectionNewSearchResultsReceived via successHandler
        expect(collectionNewSearchResultsReceived.mock.calls.length).toEqual(1)

        expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
        expect(collectionRequest.errorMessage).toEqual('')
        expect(collectionResult.collections).toEqual({
          'uuid-ABC': {title: 'ABC'},
          'uuid-123': {title: '123'},
        })
      })
    })

    describe('success path', function(){
      beforeEach(async () => {
        fetchMock.post(
          (url, opts) => url == `${BASE_URL}/search/collection`,
          mockPayload
        )
        store.dispatch(
          collectionNewSearchResultsReceived(
            10,
            [
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
            mockFacets
          )
        )
        const {collectionResult} = store.getState().search

        // results from a previous search
        expect(collectionResult.collections).toEqual({
          'uuid-XYZ': {title: 'XYZ'},
          'uuid-987': {title: '987'},
        })
      })

      describe('all submit options update request status state correctly', function(){
        allTestCases.forEach(function(testCase){
          it(`${testCase.name}`, async () => {
            await store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionResult,
            } = store.getState().search

            expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
            expect(collectionRequest.errorMessage).toEqual('')
          })
        })
      })

      describe('all new search options update the result state correctly', function(){
        standardNewSearchTestCases.forEach(function(testCase){
          it(`${testCase.name}`, async () => {
            await store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionResult,
            } = store.getState().search

            expect(collectionResult.collections).toEqual({
              'uuid-ABC': {title: 'ABC'},
              'uuid-123': {title: '123'},
            })
            expect(collectionResult.facets).toEqual(mockFacets)
            expect(collectionResult.totalCollectionCount).toEqual(10)
            expect(collectionResult.loadedCollectionCount).toEqual(2)
          })
        })
      })
    })

    describe('next page updates the result state correctly', function(){
      afterEach(() => {
        fetchMock.reset()
      })

      beforeEach(async () => {
        fetchMock.reset()
        fetchMock.post(
          (url, opts) => url == `${BASE_URL}/search/collection`,
          mockPayload
        )
      })

      it(`${submitNextPageCase.name}`, async () => {
        await store.dispatch(
          submitNextPageCase.function(...submitNextPageCase.params)
        )

        const {
          collectionRequest,
          collectionResult,
          collectionFilter,
        } = store.getState().search

        expect(collectionResult.collections).toEqual({
          'uuid-123': {title: '123'},
          'uuid-ABC': {title: 'ABC'},
        })
        expect(collectionResult.totalCollectionCount).toEqual(10)

        expect(collectionResult.loadedCollectionCount).toEqual(2)
      })
    })

    describe('failure path', function(){
      beforeEach(async () => {
        fetchMock.post(
          (url, opts) => url == `${BASE_URL}/search/collection`,
          500
        )
      })

      describe('all submit options update request status state correctly', function(){
        allTestCases.forEach(function(testCase){
          it(`${testCase.name}`, async () => {
            await store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionResult,
            } = store.getState().search

            expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
            expect(collectionRequest.errorMessage).toEqual(
              new Error('Internal Server Error')
            )
          })
        })
      })

      describe('all submit options update the result state correctly', function(){
        allTestCases.forEach(function(testCase){
          it(`${testCase.name}`, async () => {
            await store.dispatch(testCase.function(...testCase.params))

            const {
              collectionRequest,
              collectionResult,
            } = store.getState().search

            expect(collectionResult.collections).toEqual({})
            expect(collectionResult.facets).toEqual({})
            expect(collectionResult.totalCollectionCount).toEqual(0)
            expect(collectionResult.loadedCollectionCount).toEqual(0)
          })
        })
      })
    })
  })

  describe('no query or filters provided is no-op', function(){
    beforeEach(async () => {
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/collection`,
        mockPayload
      )

      // make sure there is no query text in the filters
      const {collectionFilter} = store.getState().search
      expect(collectionFilter.queryText).toEqual('')
    })

    const testCases = [
      submitSearchCase,
      {
        name: 'submit new search with query text',
        function: submitCollectionSearchWithQueryText,
        params: [ mockHistory, '' ],
      },
      {
        name: 'submit new search with filters',
        function: submitCollectionSearchWithFilter,
        params: [ mockHistory, {} ],
      },
      submitNextPageCase,
    ]

    describe('all submit options throw an error before the fetch', function(){
      testCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {collectionRequest, collectionResult} = store.getState().search

          expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
          expect(collectionRequest.errorMessage).toEqual('Invalid Request')
        })
      })
    })
  })
})
