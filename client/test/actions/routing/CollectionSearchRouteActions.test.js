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
  collectionMoreResultsRequested,
  collectionNewSearchResultsReceived,
  collectionUpdateQueryText,
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

    // set default queryText
    store.dispatch(collectionUpdateQueryText('demo'))
    const {collectionFilter} = store.getState().search
    expect(collectionFilter.queryText).toEqual('demo')
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
      search: '?s=1998',
    },
  }
  const submitNextPageCase = {
    name: 'submit next page',
    function: submitCollectionSearchNextPage,
    params: [],
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

  describe('all submit options behave the same when a detail request is already in flight', function(){
    beforeEach(async () => {
      //setup send something into flight first
      store.dispatch(collectionMoreResultsRequested())
      const {collectionRequest, collectionFilter} = store.getState().search
      expect(collectionRequest.inFlight).toBeTruthy()
      expect(collectionFilter.pageOffset).toBe(20)
    })

    allTestCases.forEach(function(testCase){
      it(`${testCase.name} does not continue with a submit request`, function(){
        store.dispatch(testCase.function(...testCase.params))

        expect(historyPushCallCount).toEqual(0) // all new searches push a new history request right now (although next page would not)
        expect(store.getState().search.collectionFilter.pageOffset).toBe(20) // but just in case, this definitely should always be reset to 0, or changed to 40
      })
    })
  })

  describe('prefetch actions', function(){
    beforeEach(async () => {
      // pretend next page has been triggered and completed, so that pageOffset has been modified by a prior search
      store.dispatch(collectionMoreResultsRequested())
      store.dispatch(collectionNewSearchResultsReceived(0, [], {}))
      const {collectionRequest, collectionFilter} = store.getState().search
      expect(collectionRequest.inFlight).toBeFalsy()
      expect(collectionFilter.pageOffset).toEqual(20)
    })

    describe('all submit options update the state correctly', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name}`, function(){
          store.dispatch(testCase.function(...testCase.params))

          const {collectionRequest, collectionFilter} = store.getState().search

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
        expect(collectionFilter.pageOffset).toEqual(40)
      })
    })

    describe('each submit function treats the URL differently', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name} updates the URL`, function(){
          store.dispatch(testCase.function(...testCase.params))

          const {collectionRequest, collectionFilter} = store.getState().search

          expect(historyPushCallCount).toEqual(1)
          expect(history_input).toEqual(testCase.expectedURL)
        })
      })

      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name} does not update history if no change`, function(){
          mockHistory.location = testCase.expectedURL
          store.dispatch(testCase.function(...testCase.params))

          const {collectionRequest, collectionFilter} = store.getState().search

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

          const {collectionRequest, collectionResult} = store.getState().search

          expect(collectionRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
          expect(collectionRequest.errorMessage).toEqual('')
        })
      })
    })

    describe('all new search options update the result state correctly', function(){
      standardNewSearchTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {collectionRequest, collectionResult} = store.getState().search

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

    describe('next page updates the result state correctly', function(){
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
          'uuid-ABC': {title: 'ABC'},
          'uuid-123': {title: '123'},
          'uuid-XYZ': {title: 'XYZ'},
          'uuid-987': {title: '987'},
        })
        expect(collectionResult.facets).toEqual(mockFacets)
        expect(collectionResult.totalCollectionCount).toEqual(10)
        expect(collectionResult.loadedCollectionCount).toEqual(4)
      })
    })
  })

  describe('failure path', function(){
    beforeEach(async () => {
      fetchMock.post((url, opts) => url == `${BASE_URL}/search/collection`, 500)
    })

    describe('all submit options update request status state correctly', function(){
      allTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

          const {collectionRequest, collectionResult} = store.getState().search

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

          const {collectionRequest, collectionResult} = store.getState().search

          expect(collectionResult.collections).toEqual({})
          expect(collectionResult.facets).toEqual({})
          expect(collectionResult.totalCollectionCount).toEqual(0)
          expect(collectionResult.loadedCollectionCount).toEqual(0)
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

      store.dispatch(collectionUpdateQueryText('')) // clear the state query filter

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
