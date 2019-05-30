import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {
  submitGranuleSearch,
  submitGranuleSearchWithFilter,
  submitGranuleSearchNextPage,
} from '../../../src/actions/routing/GranuleSearchRouteActions'
import {
  // used to set up pre-test conditions
  granuleNewSearchRequested,
  granuleNewSearchResultsReceived,
  granuleMoreResultsRequested,
  granuleUpdateDateRange,
} from '../../../src/actions/routing/GranuleSearchStateActions'

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
    historyPushCallCount = 0
    mockHistory.location = {pathname: 'test', search: null}
    // reset store to initial conditions
    await store.dispatch(resetStore())

    store.dispatch(granuleNewSearchRequested('original-uuid')) // TODO replace with await store.dispatch(granuleNewSearchRequested('original-uuid')) ??
    store.dispatch(granuleNewSearchResultsReceived(0, [], {}))
    const {granuleRequest, granuleFilter} = store.getState().search
    expect(granuleRequest.inFlight).toBeFalsy()
    expect(granuleFilter.selectedIds).toEqual([ 'original-uuid' ])
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
      search: null,
    },
  }
  const submitSearchWithFilterCase = {
    name: 'submit new search with filters',
    function: submitGranuleSearchWithFilter,
    params: [ mockHistory, 'parent-uuid', {startDateTime: '1998'} ],
    expectedURL: {
      pathname: '/collections/granules/parent-uuid',
      search: '?s=1998',
    },
  }
  const submitNextPageCase = {
    name: 'submit next page',
    function: submitGranuleSearchNextPage,
    params: [],
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
  })

  describe('all submit options behave the same when a detail request is already in flight', function(){
    beforeEach(async () => {
      //setup send something into flight first
      store.dispatch(granuleMoreResultsRequested())
      const {granuleRequest, granuleFilter} = store.getState().search
      expect(granuleRequest.inFlight).toBeTruthy()
      expect(granuleFilter.pageOffset).toBe(20)
    })

    allTestCases.forEach(function(testCase){
      it(`${testCase.name} does not continue with a submit request`, function(){
        store.dispatch(testCase.function(...testCase.params))

        expect(historyPushCallCount).toEqual(0) // all new searches push a new history request right now (although next page would not)
        expect(store.getState().search.granuleFilter.pageOffset).toBe(20) // but just in case, this definitely should always be reset to 0, or changed to 40
      })
    })
  })

  describe('prefetch actions', function(){
    beforeEach(async () => {
      // pretend next page has been triggered and completed, so that pageOffset has been modified by a prior search
      store.dispatch(granuleMoreResultsRequested())
      store.dispatch(granuleNewSearchResultsReceived(0, [], {}))
      const {granuleRequest, granuleFilter} = store.getState().search
      expect(granuleRequest.inFlight).toBeFalsy()
      expect(granuleFilter.pageOffset).toEqual(20)
    })

    describe('all submit options update the state correctly', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name}`, function(){
          store.dispatch(testCase.function(...testCase.params))

          const {granuleRequest, granuleFilter} = store.getState().search

          expect(granuleFilter.selectedIds).toEqual([ 'parent-uuid' ])
          expect(granuleRequest.inFlight).toBeTruthy()
          expect(granuleFilter.pageOffset).toEqual(0)
        })
      })

      it(`${submitNextPageCase.name}`, function(){
        store.dispatch(
          submitNextPageCase.function(...submitNextPageCase.params)
        )

        const {granuleRequest, granuleFilter} = store.getState().search

        expect(granuleFilter.selectedIds).toEqual([ 'original-uuid' ])
        expect(granuleRequest.inFlight).toBeTruthy()
        expect(granuleFilter.pageOffset).toEqual(40)
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

  describe('success path', function(){
    beforeEach(async () => {
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayload
      )
      store.dispatch(
        granuleNewSearchResultsReceived(
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

    describe('next page updates the result state correctly', function(){
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
          'uuid-XYZ': {title: 'XYZ'},
          'uuid-987': {title: '987'},
        })
        expect(granuleResult.facets).toEqual(mockFacets)
        expect(granuleResult.totalGranuleCount).toEqual(10)
        expect(granuleResult.loadedGranuleCount).toEqual(4)
      })
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
