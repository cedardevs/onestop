import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'
import Immutable from 'seamless-immutable'
import * as spyableActions from '../../../src/actions/routing/GranuleSearchStateActions'
import * as spyOnQueryUtils from '../../../src/utils/queryUtils'

import {
  submitGranuleSearch,
  submitGranuleSearchWithFilter,
  submitGranuleSearchWithPage,
  submitGranuleSearchForCart,
} from '../../../src/actions/routing/GranuleSearchRouteActions'
import {
  // used to set up pre-test conditions
  granuleNewSearchRequested,
  granuleResultsPageRequested,
  granuleNewSearchResultsReceived,
  granuleUpdateDateRange,
  granulesForCartRequested,
  granulesForCartResultsReceived,
  granulesForCartError,
  granulesForCartClearError,
} from '../../../src/actions/routing/GranuleSearchStateActions'
import {
  warningExceedsMaxAddition,
  warningNothingNew,
  warningOverflow,
  warningOverflowFromEmpty,
} from '../../../src/utils/cartUtils'

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

describe('granule search actions', function(){
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

  const mockPayloadCart1 = {
    data: [
      {
        id: 'uuid-ABC',
        attributes: {
          title: 'ABC',
        },
      },
      {
        id: 'uuid-DEF',
        attributes: {
          title: 'DEF',
        },
      },
      {
        id: 'uuid-GHI',
        attributes: {
          title: 'GHI',
        },
      },
    ],
    meta: {
      facets: mockFacets,
      total: 3,
    },
  }
  const mockPayloadCart2 = {
    data: [
      {
        id: 'uuid-123',
        attributes: {
          title: '123',
        },
      },
      {
        id: 'uuid-456',
        attributes: {
          title: '456',
        },
      },
      {
        id: 'uuid-789',
        attributes: {
          title: '789',
        },
      },
    ],
    meta: {
      facets: mockFacets,
      total: 3,
    },
  }

  beforeEach(async () => {
    history_input = {}
    historyPushCallCount = 0
    mockHistory.location = {pathname: 'test', search: null}
    // reset store to initial conditions
    await store.dispatch(resetStore())

    store.dispatch(granuleNewSearchRequested('original-uuid'))
    store.dispatch(granuleNewSearchResultsReceived([], {}, 0))
    const {granuleRequest, granuleFilter} = store.getState().search
    expect(granuleRequest.inFlight).toBeFalsy()
    expect(granuleFilter.selectedCollectionIds).toEqual([ 'original-uuid' ])
  })

  afterEach(() => {
    fetchMock.reset()
  })

  const submitSearchCase = {
    name: 'submit new search',
    function: submitGranuleSearch,
    params: [ mockHistory, 'parent-uuid' ],
    expectedURL: {
      pathname: '/collections/granules/parent-uuid',
      search: '',
    },
  }
  const submitSearchWithFilterCase = {
    name: 'submit new search with filters',
    function: submitGranuleSearchWithFilter,
    params: [ mockHistory, 'parent-uuid', {startDateTime: '1998'} ],
    expectedURL: {
      pathname: '/collections/granules/parent-uuid',
      search: '?tr=i&s=1998', // default intersects relation
    },
  }
  const submitNextPageCase = {
    name: 'submit next page',
    function: submitGranuleSearchWithPage,
    params: [ 2, 2 ],
  }

  const submitGranuleSearchForCartCase = {
    name: 'submit granule search for cart',
    function: submitGranuleSearchForCart,
    params: [
      mockHistory,
      {
        geoJSON: null,
        startDateTime: null,
        endDateTime: null,
        selectedFacets: {},
        selectedCollectionIds: [ 'J5pZkWwBHQMyuNGWG_Lh' ],
        excludeGlobal: null,
        pageOffset: 0,
      },
    ],
  }

  const standardNewSearchTestCases = [
    submitSearchCase,
    submitSearchWithFilterCase,
  ]

  const allTestCases = [
    submitSearchCase,
    submitSearchWithFilterCase,
    submitNextPageCase,
  ]

  describe('all submit options behave the same with invalid id param', function(){
    const cases = [
      {
        name: 'id null, submit with filters',
        function: submitGranuleSearchWithFilter,
        params: [ mockHistory, null, {} ],
      },
      {
        name: 'id null, submit',
        function: submitGranuleSearch,
        params: [ mockHistory, null ],
      },
      {
        name: 'id empty string, submit with filters',
        function: submitGranuleSearchWithFilter,
        params: [ mockHistory, '', {} ],
      },
      {
        name: 'id string, submit',
        function: submitGranuleSearch,
        params: [ mockHistory, '' ],
      },
    ]

    cases.forEach(function(testCase){
      it(`${testCase.name} does not continue with a submit request`, function(){
        store.dispatch(testCase.function(...testCase.params))

        const {granuleRequest} = store.getState().search
        expect(granuleRequest.inFlight).toBeFalsy()
      })
    })
  })

  describe('new searches treat filters differently', function(){
    beforeEach(async () => {
      // set up existing filters with lazily formatted dates
      store.dispatch(granuleUpdateDateRange('2017', '2018'))
      const {granuleFilter} = store.getState().search
      expect(granuleFilter.startDateTime).toEqual('2017')
      expect(granuleFilter.endDateTime).toEqual('2018')
    })

    it(`${submitSearchCase.name} does not change existing filters`, function(){
      store.dispatch(submitSearchCase.function(...submitSearchCase.params))

      const granuleFilter = store.getState().search.granuleFilter
      expect(granuleFilter.startDateTime).toEqual('2017')
      expect(granuleFilter.endDateTime).toEqual('2018')
    })

    it(`${submitSearchWithFilterCase.name} resets existing filters`, function(){
      store.dispatch(
        submitSearchWithFilterCase.function(
          ...submitSearchWithFilterCase.params
        )
      )

      const granuleFilter = store.getState().search.granuleFilter
      expect(granuleFilter.startDateTime).toEqual('1998')
      expect(granuleFilter.endDateTime).toBeNull()
    })

    it(`${submitGranuleSearchForCartCase.name} does not change existing filters`, function(){
      store.dispatch(
        submitGranuleSearchForCartCase.function(
          ...submitGranuleSearchForCartCase.params
        )
      )

      const granuleFilter = store.getState().search.granuleFilter
      expect(granuleFilter.startDateTime).toEqual('2017')
      expect(granuleFilter.endDateTime).toEqual('2018')
    })
  })

  describe('submit overwrites inFlight', function(){
    beforeEach(async () => {
      //setup send something into flight first
      store.dispatch(submitGranuleSearchWithPage(20, 20))
      const {granuleRequest, granuleFilter} = store.getState().search
      expect(granuleRequest.inFlight).toBeTruthy()
      expect(granuleFilter.pageOffset).toBe(20)
      expect(granuleFilter.pageSize).toEqual(20)
    })

    it(`${submitNextPageCase.name} replaces in flight request`, function(){
      store.dispatch(submitNextPageCase.function(...submitNextPageCase.params))

      expect(historyPushCallCount).toEqual(0) // all new searches push a new history request right now (although next page would not)
      expect(
        store.getState().search.granuleFilter.selectedCollectionIds
      ).toEqual([ 'original-uuid' ])
      expect(store.getState().search.granuleFilter.pageOffset).toBe(2)
      expect(store.getState().search.granuleFilter.pageSize).toBe(2) // but just in case, this definitely should always be reset to 0, or changed to 40
    })

    standardNewSearchTestCases.forEach(function(testCase){
      it(`${testCase.name} replaces in flight request`, function(){
        store.dispatch(testCase.function(...testCase.params))

        expect(historyPushCallCount).toEqual(1) // all new searches push a new history request right now (although next page would not)
        expect(store.getState().search.granuleFilter.pageOffset).toBe(0) // but just in case, this definitely should always be reset to 0, or changed to 40
      })
    })
  })

  describe('prefetch actions', function(){
    beforeEach(async () => {
      // pretend next page has been triggered and completed, so that pageOffset has been modified by a prior search
      store.dispatch(granuleResultsPageRequested(20, 20))
      store.dispatch(granuleNewSearchResultsReceived([], {}, 0))
      const {granuleRequest, granuleFilter} = store.getState().search
      expect(granuleRequest.inFlight).toBeFalsy()
      expect(granuleFilter.pageOffset).toEqual(20)
      expect(granuleFilter.pageSize).toEqual(20)
    })

    describe('all submit options update the state correctly', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name}`, function(){
          store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleFilter} = store.getState().search

          expect(granuleFilter.selectedCollectionIds).toEqual([ 'parent-uuid' ])
          expect(granuleRequest.inFlight).toBeTruthy()
          expect(granuleFilter.pageOffset).toEqual(0)
        })
      })

      it(`${submitNextPageCase.name}`, function(){
        store.dispatch(
          submitNextPageCase.function(...submitNextPageCase.params)
        )

        const {granuleRequest, granuleFilter} = store.getState().search

        expect(granuleFilter.selectedCollectionIds).toEqual([ 'original-uuid' ])
        expect(granuleRequest.inFlight).toBeTruthy()
        expect(granuleFilter.pageOffset).toEqual(2)
      })
    })

    describe('each submit function treats the URL differently', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name} updates the URL`, function(){
          store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleFilter} = store.getState().search

          expect(historyPushCallCount).toEqual(1)
          expect(history_input).toEqual(testCase.expectedURL)
        })
      })

      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name} does not update history if no change`, function(){
          mockHistory.location = testCase.expectedURL
          store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleFilter} = store.getState().search

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
    const granuleResultsPageReceived = jest.spyOn(
      spyableActions,
      'granuleResultsPageReceived'
    )
    const granuleNewSearchResultsReceived = jest.spyOn(
      spyableActions,
      'granuleNewSearchResultsReceived'
    )
    const granuleSearchError = jest.spyOn(spyableActions, 'granuleSearchError')
    beforeEach(async () => {
      assembleSearchRequest.mockClear()
      granuleNewSearchResultsReceived.mockClear()
      granuleResultsPageReceived.mockClear()
      granuleSearchError.mockClear()
    })
    afterAll(async () => {
      assembleSearchRequest.mockClear()
      granuleNewSearchResultsReceived.mockClear()
      granuleResultsPageReceived.mockClear()
      granuleSearchError.mockClear()
      // restore the original (non-mocked) implementation:
      assembleSearchRequest.mockRestore()
      granuleNewSearchResultsReceived.mockRestore()
      granuleResultsPageReceived.mockRestore()
      granuleSearchError.mockRestore()
    })

    test('new filter interrupted by next page leaves the UI (redux store) in consistent state', async () => {
      fetchMock
        // mock the results of the first search request:
        .postOnce((url, opts) => url == `${BASE_URL}/search/granule`, {
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
          (url, opts) => url == `${BASE_URL}/search/granule`,
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
        const {granuleRequest} = store.getState().search
        expect(granuleRequest.inFlight).toBeTruthy()
      }

      // which is interrupted by a Next Page query
      await store.dispatch(
        submitNextPageCase.function(...submitNextPageCase.params)
      )

      // allow the initial request to resolve
      await promise

      const {granuleRequest, granuleResult} = store.getState().search

      // we made 2 fetches
      expect(fetchMock.calls().length).toEqual(2)
      expect(assembleSearchRequest.mock.calls.length).toEqual(2)
      expect(granuleNewSearchResultsReceived.mock.calls.length).toEqual(0) // first request never completed and called this!
      expect(granuleResultsPageReceived.mock.calls.length).toEqual(1)

      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual('')
      expect(granuleResult.granules).toEqual({
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
          {
            type: 'collection',
            values: [ 'parent-uuid' ],
          },
        ],
        page: {
          max: 2,
          offset: 2,
        },
        queries: [],
      }) // create the request with both the recently applied filter (for the request that did not complete) PLUS the next page offset
      expect(granuleResultsPageReceived.mock.results[0].value.granules).toEqual(
        mockPayload.data
      ) // and not "INTERRUPTED" payload
    })

    test('new search request which errors', async () => {
      fetchMock
        // mock the results of the first search request:
        .postOnce((url, opts) => url == `${BASE_URL}/search/granule`, 400)
        // mock the results of the second search request:
        .postOnce((url, opts) => url == `${BASE_URL}/search/granule`, 500)
      //setup send something into flight first

      // manually trigger the first layer of the dispatch (preflight actions), while storing the promise to resolve later. This mimics a long-running initial query that will be interrupted by a subsequent query
      const promise = submitSearchCase.function(...submitSearchCase.params)(
        store.dispatch,
        store.getState
      )

      {
        // keep scoped
        const {granuleRequest} = store.getState().search
        expect(granuleRequest.inFlight).toBeTruthy()
      }

      await store.dispatch(
        submitSearchCase.function(...submitSearchCase.params)
      )

      // allow the initial request to resolve
      await promise

      const {granuleRequest, granuleResult} = store.getState().search

      // we made 2 fetches
      expect(fetchMock.calls().length).toEqual(2)
      // but only one granuleSearchError via errorHandler
      expect(granuleSearchError.mock.calls.length).toEqual(1)

      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual(
        new Error('Internal Server Error')
      )
      expect(granuleResult.granules).toEqual({})
    })

    test('new search request success', async () => {
      fetchMock
        // mock the results of the first search request:
        .postOnce((url, opts) => url == `${BASE_URL}/search/granule`, {
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
          (url, opts) => url == `${BASE_URL}/search/granule`,
          mockPayload
        )
      //setup send something into flight first

      // manually trigger the first layer of the dispatch (preflight actions), while storing the promise to resolve later. This mimics a long-running initial query that will be interrupted by a subsequent query
      const promise = submitSearchCase.function(...submitSearchCase.params)(
        store.dispatch,
        store.getState
      )

      {
        // keep scoped
        const {granuleRequest} = store.getState().search
        expect(granuleRequest.inFlight).toBeTruthy()
      }

      await store.dispatch(
        submitSearchCase.function(...submitSearchCase.params)
      )

      // allow the initial request to resolve (red-green testing: without the fetch checking the abort controller status, this would cause the number of calls to granuleNewSearchResultsReceived to be incorrect)
      await promise

      const {granuleRequest, granuleResult} = store.getState().search

      // we made 2 fetches
      expect(fetchMock.calls().length).toEqual(2)
      // but only one granuleNewSearchResultsReceived via successHandler
      expect(granuleNewSearchResultsReceived.mock.calls.length).toEqual(1)

      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual('')
      expect(granuleResult.granules).toEqual({
        'uuid-ABC': {title: 'ABC'},
        'uuid-123': {title: '123'},
      })
    })
  })

  describe('success path', function(){
    beforeEach(async () => {
      fetchMock.reset()
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayload
      )
      store.dispatch(
        granuleNewSearchResultsReceived(
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
          mockFacets,
          10
        )
      )
      const {granuleResult} = store.getState().search

      // results from a previous search
      expect(granuleResult.granules).toEqual({
        'uuid-XYZ': {title: 'XYZ'},
        'uuid-987': {title: '987'},
      })
    })

    describe('all submit options update request status state correctly', function(){
      allTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleResult} = store.getState().search

          expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
          expect(granuleRequest.errorMessage).toEqual('')
        })
      })
    })

    describe('all new search options update the result state correctly', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleResult} = store.getState().search

          expect(granuleResult.granules).toEqual({
            'uuid-ABC': {title: 'ABC'},
            'uuid-123': {title: '123'},
          })
          expect(granuleResult.facets).toEqual(mockFacets)
          expect(granuleResult.totalGranuleCount).toEqual(10)
          expect(granuleResult.loadedGranuleCount).toEqual(2)
        })
      })
    })
  })

  describe('next page updates the result state correctly', function(){
    beforeEach(async () => {
      fetchMock.reset()
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayload
      )
    })
    it(`${submitNextPageCase.name}`, async () => {
      await store.dispatch(
        submitNextPageCase.function(...submitNextPageCase.params)
      )

      const {
        granuleRequest,
        granuleResult,
        collectionFilter,
      } = store.getState().search

      expect(granuleResult.granules).toEqual({
        'uuid-ABC': {title: 'ABC'},
        'uuid-123': {title: '123'},
      })
      expect(granuleResult.totalGranuleCount).toEqual(10)
      expect(granuleResult.loadedGranuleCount).toEqual(2)
    })
  })
  describe('Add Filtered Granules to Cart Actions', function(){
    let mockMaxCartAdditions = 3
    let mockCartCapacity = 5

    let granuleFilterState = Immutable({
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      selectedCollectionIds: [ 'original-uuid' ],
      excludeGlobal: null,
      pageOffset: 0,
    })

    beforeEach(async () => {
      // reset local storage before each test
      localStorage.clear()

      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayloadCart1
      )

      history_input = {}
      historyPushCallCount = 0
      mockHistory.location = {
        pathname: '/collections/granules/original-uuid',
        search: null,
      }

      // reset store to initial conditions
      await store.dispatch(resetStore())
    })

    afterEach(() => {
      fetchMock.reset()
    })

    test('in flight flagged on request, deflagged on results', async () => {
      const stateBefore = store.getState()
      expect(stateBefore.search.granuleRequest.cartGranulesInFlight).toBeFalsy()
      await store.dispatch(granulesForCartRequested())
      const stateAfterRequest = store.getState()
      expect(
        stateAfterRequest.search.granuleRequest.cartGranulesInFlight
      ).toBeTruthy()
      await store.dispatch(granulesForCartResultsReceived([], 0))
      const stateAfterResults = store.getState()
      expect(
        stateAfterResults.search.granuleRequest.cartGranulesInFlight
      ).toBeFalsy()
    })

    test('in flight flagged on request, deflagged on error', async () => {
      const stateBefore = store.getState()
      expect(stateBefore.search.granuleRequest.cartGranulesInFlight).toBeFalsy()
      await store.dispatch(granulesForCartRequested())
      const stateAfterRequest = store.getState()
      expect(
        stateAfterRequest.search.granuleRequest.cartGranulesInFlight
      ).toBeTruthy()
      await store.dispatch(granulesForCartError('warning'))
      const stateAfterError = store.getState()
      expect(
        stateAfterError.search.granuleRequest.cartGranulesInFlight
      ).toBeFalsy()
    })

    test('success path', async () => {
      const stateBefore = store.getState()

      // initially there are no selected granules in the cart
      expect(stateBefore.cart.selectedGranules).toStrictEqual({})

      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          mockMaxCartAdditions,
          mockCartCapacity
        )
      )

      const stateAfter = store.getState()

      // after the request, we expect the cart selected granules to be the same length as the mock granule response data
      expect(Object.keys(stateAfter.cart.selectedGranules).length).toBe(
        mockPayloadCart1.data.length
      )
    })

    test('cart capacity overflow', async () => {
      const stateBefore = store.getState()

      // initially there are no selected granules in the cart
      expect(stateBefore.cart.selectedGranules).toStrictEqual({})

      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          mockMaxCartAdditions,
          mockCartCapacity
        )
      )

      const stateAfter = store.getState()

      // after the first request, we expect the cart selected granules to be the same length as the mock granule response data
      // because 3 items should not overflow the cart yet (that limit is dummied out to 5)
      expect(Object.keys(stateAfter.cart.selectedGranules).length).toBe(
        mockPayloadCart1.data.length
      )

      expect(
        Object.keys(JSON.parse(localStorage.cart).selectedGranules).length
      ).toBe(mockPayloadCart1.data.length)

      // mock another set of unique ID granules to add to cart which will cause the overflow
      fetchMock.reset()
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayloadCart2
      )
      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          mockMaxCartAdditions,
          mockCartCapacity
        )
      )

      const stateAfter2 = store.getState()
      // after the second request, we expect a warning
      expect(stateAfter2.cart.error).toEqual(
        warningOverflow(mockPayloadCart2.data.length, mockCartCapacity)
      )

      // the amount of items in the redux store and in local storage should be the same as after th first request
      expect(Object.keys(stateAfter2.cart.selectedGranules).length).toBe(
        mockPayloadCart1.data.length
      )
      expect(
        Object.keys(JSON.parse(localStorage.cart).selectedGranules).length
      ).toBe(mockPayloadCart1.data.length)

      // dispatching `granulesForCartClearError()` after a warning has been set in the cart reducer, clears it
      await store.dispatch(granulesForCartClearError())
      const stateAfter3 = store.getState()
      expect(stateAfter3.cart.error).toBeNull()
    })

    test('cart capacity overflow from empty', async () => {
      let lowCartCapacity = 2
      const stateBefore = store.getState()

      // initially there are no selected granules in the cart
      expect(stateBefore.cart.selectedGranules).toStrictEqual({})

      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          mockMaxCartAdditions,
          lowCartCapacity
        )
      )

      const stateAfter = store.getState()

      // after the first request, we expect the cart selected granules to be empty because we made the capacity 2
      // and we requested a filter with 3 granules
      expect(Object.keys(stateAfter.cart.selectedGranules).length).toBe(0)
      // if we've never added anything to local storage, the key 'selectedGranules' may not even exist
      // so testing it's length to be 0 would be pointless
      if (localStorage.cart) {
        expect(
          Object.keys(JSON.parse(localStorage.cart).selectedGranules).length
        ).toBe(0)
      }

      // after the request, we expect a warning
      expect(stateAfter.cart.error).toEqual(
        warningOverflowFromEmpty(mockPayloadCart1.data.length, lowCartCapacity)
      )
    })

    test('nothing new to add warning', async () => {
      const stateBefore = store.getState()

      // initially there are no selected granules in the cart
      expect(stateBefore.cart.selectedGranules).toStrictEqual({})

      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          mockMaxCartAdditions,
          mockCartCapacity
        )
      )

      const stateAfter = store.getState()

      // after the request, we expect the cart selected granules to be the same length as the mock granule response data
      expect(Object.keys(stateAfter.cart.selectedGranules).length).toBe(
        mockPayloadCart1.data.length
      )

      // if we repeat the same request, we would be attempting to add the same exact IDs to the cart and should get
      // a warning, the same request can be mocked with the same previous fetch mock, so we don't need to do anything
      // special to mock the second request here
      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          mockMaxCartAdditions,
          mockCartCapacity
        )
      )

      const stateAfter2 = store.getState()

      // the amount of items in the redux store and in local storage should be the same as after th first request
      expect(Object.keys(stateAfter2.cart.selectedGranules).length).toBe(
        mockPayloadCart1.data.length
      )
      expect(
        Object.keys(JSON.parse(localStorage.cart).selectedGranules).length
      ).toBe(mockPayloadCart1.data.length)

      // after the second request, we expect a warning
      expect(stateAfter2.cart.error).toEqual(warningNothingNew())
    })

    test('exceeds max cart additions warning', async () => {
      // mock a non-cart granule search response so that the total granule count of our filter gets set for comparison
      // prior to any cart submission action
      fetchMock.reset()
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule/parent-uuid`,
        mockPayloadCart1
      )
      store.dispatch(
        granuleNewSearchResultsReceived(
          mockPayloadCart1.data,
          mockPayloadCart1.meta.facets,
          mockPayloadCart1.meta.total
        )
      )

      // reset and revert to our previous cart addition request mock
      fetchMock.reset()
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayloadCart1
      )

      // artificially set the max cart additions to something that will trigger the warning
      let lowMaxCartAdditions = 2
      const stateBefore = store.getState()

      // initially there are no selected granules in the cart
      expect(stateBefore.cart.selectedGranules).toStrictEqual({})

      await store.dispatch(
        submitGranuleSearchForCart(
          mockHistory,
          granuleFilterState,
          lowMaxCartAdditions,
          mockCartCapacity
        )
      )

      const stateAfter = store.getState()

      // after the first request, we expect the cart selected granules to be empty because we made the max additions 2
      // and we requested a filter with 3 granules
      expect(Object.keys(stateAfter.cart.selectedGranules).length).toBe(0)
      // if we've never added anything to local storage, the key 'selectedGranules' may not even exist
      // so testing it's length to be 0 would be pointless
      if (localStorage.cart) {
        expect(
          Object.keys(JSON.parse(localStorage.cart).selectedGranules).length
        ).toBe(0)
      }

      // after the second request, we expect a warning
      expect(stateAfter.cart.error).toEqual(
        warningExceedsMaxAddition(
          mockPayloadCart1.meta.total,
          lowMaxCartAdditions
        )
      )
    })
  })

  describe('failure path', function(){
    beforeEach(async () => {
      fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 500)
    })

    describe('all submit options update request status state correctly', function(){
      allTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleResult} = store.getState().search

          expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
          expect(granuleRequest.errorMessage).toEqual(
            new Error('Internal Server Error')
          )
        })
      })
    })

    describe('all submit options update the result state correctly', function(){
      allTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleResult} = store.getState().search

          expect(granuleResult.granules).toEqual({})
          expect(granuleResult.facets).toEqual({})
          expect(granuleResult.totalGranuleCount).toEqual(0)
          expect(granuleResult.loadedGranuleCount).toEqual(0)
        })
      })
    })
  })
})
