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

  const submitAndNavigateCase = {
    name: 'submit and navigate',
    function: submitCollectionDetailAndUpdateUrl,
    params: [ mockHistory, 'uuid-ABC', {} ],
  }

  const submitCase = {
    name: 'submit',
    function: submitCollectionDetail,
    params: [ 'uuid-ABC', {} ],
  }

  const standardTestCases = [ submitAndNavigateCase, submitCase ]

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

  describe('all submit options behave the same with invalid id param', function(){
    const cases = [
      {
        name: 'id null, submit and navigate',
        function: submitCollectionDetailAndUpdateUrl,
        params: [ mockHistory, null, {} ],
      },
      {
        name: 'id null, submit',
        function: submitCollectionDetail,
        params: [ null, {} ],
      },
      {
        name: 'id empty string, submit and navigate',
        function: submitCollectionDetailAndUpdateUrl,
        params: [ mockHistory, '', {} ],
      },
      {
        name: 'id string, submit',
        function: submitCollectionDetail,
        params: [ '', {} ],
      },
    ]

    cases.forEach(function(testCase){
      it(`${testCase.name} does not continue with a submit request`, function(){
        store.dispatch(testCase.function(...testCase.params))

        const {collectionDetailRequest} = store.getState().search
        expect(collectionDetailRequest.inFlight).toBeFalsy()
      })
    })
  })

  describe('all submit options behave the same when a detail request is already in flight', function(){
    beforeEach(async () => {
      store.dispatch(collectionDetailRequested('uuid-XYZ'))
    })

    standardTestCases.forEach(function(testCase){
      it(`${testCase.name} does not continue with a submit request`, function(){
        store.dispatch(testCase.function(...testCase.params))
        expect(
          store.getState().search.collectionDetailRequest.requestedID
        ).toEqual('uuid-XYZ') // this would be uuid-ABC if the request went through
      })
    })
  })

  describe('all submit options behave the same when a granule count request is already in flight', function(){
    beforeEach(async () => {
      store.dispatch(granuleMatchingCountRequested())
    })

    standardTestCases.forEach(function(testCase){
      it(`${testCase.name} does not continue with a submit request`, function(){
        store.dispatch(testCase.function(...testCase.params))

        const {collectionDetailRequest} = store.getState().search
        expect(collectionDetailRequest.inFlight).toBeFalsy()
        expect(collectionDetailRequest.requestedID).toEqual(null) // this would be uuid-ABC if the request went through
      })
    })
  })

  describe('prefetch actions', function(){
    describe('all submit options update the state correctly', function(){
      standardTestCases.forEach(function(testCase){
        it(`${testCase.name}`, function(){
          // starts out not in flight
          expect(
            store.getState().search.collectionDetailRequest.inFlight
          ).toBeFalsy()

          store.dispatch(testCase.function(...testCase.params))

          const {
            collectionDetailRequest,
            collectionDetailFilter,
          } = store.getState().search

          expect(collectionDetailRequest.inFlight).toBeTruthy()
          expect(collectionDetailRequest.backgroundInFlight).toBeTruthy()
          expect(collectionDetailRequest.requestedID).toEqual('uuid-ABC')
          expect(collectionDetailFilter.selectedIds).toEqual([ 'uuid-ABC' ])
          // TODO worth asserting other filter state, or let it be assumed by the tests directly against that reducer + action?
        })
      })
    })

    describe('each submit function treats the URL differently', function(){
      it(`${submitAndNavigateCase.name} updates the URL`, function(){
        store.dispatch(
          submitAndNavigateCase.function(...submitAndNavigateCase.params)
        )

        expect(historyPushCallCount).toEqual(1)

        expect(history_input).toEqual({
          pathname: '/collections/details/uuid-ABC',
          search: null,
        })
      })

      it(`${submitAndNavigateCase.name} does not update history if no change`, function(){
        // start already at that URL
        mockHistory.location = {
          pathname: '/collections/details/uuid-ABC',
          search: null,
        }

        store.dispatch(
          submitAndNavigateCase.function(...submitAndNavigateCase.params)
        )

        expect(historyPushCallCount).toEqual(0)
      })

      it(`${submitCase.name} does not change the URL`, function(){
        store.dispatch(submitCase.function(...submitCase.params))

        expect(historyPushCallCount).toEqual(0)
      })
    })
  })

  describe('success path', function(){
    beforeEach(async () => {
      fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
      fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
        meta: {
          total: 10,
        },
      })
    })

    describe('all submit options update the state correctly', function(){
      standardTestCases.forEach(function(testCase){
        it(`${testCase.name}`, async () => {
          await store.dispatch(testCase.function(...testCase.params))

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
      })
    })

    describe('each submit function treats the URL differently', function(){
      // these tests mostly duplicate the prefetch URL checks, but in the context of waiting for the request to fully resolve

      it(`${submitAndNavigateCase.name} updates the URL`, async () => {
        await store.dispatch(
          submitAndNavigateCase.function(...submitAndNavigateCase.params)
        )

        expect(historyPushCallCount).toEqual(1)

        expect(history_input).toEqual({
          pathname: '/collections/details/uuid-ABC',
          search: null,
        })
      })

      it(`${submitCase.name} does not change the URL`, async () => {
        await store.dispatch(submitCase.function(...submitCase.params))

        expect(historyPushCallCount).toEqual(0)
      })
    })
  })

  describe('failure path', function(){
    describe('when detail request fails', function(){
      beforeEach(async () => {
        fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, 404)
        fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, {
          meta: {
            total: 10,
          },
        })
      })

      describe('all submit options update the state correctly', function(){
        standardTestCases.forEach(function(testCase){
          it(`${testCase.name}`, async () => {
            await store.dispatch(testCase.function(...testCase.params))

            const {
              collectionDetailRequest,
              collectionDetailResult,
            } = store.getState().search
            expect(collectionDetailRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
            expect(collectionDetailRequest.errorMessage).toEqual(
              new Error('Not Found')
            )
            expect(collectionDetailResult.collection).toBeNull()
            expect(collectionDetailRequest.backgroundInFlight).toBeFalsy() // after completing the request, this should be reset too
          })
        })
      })
    })

    describe('when granule count request fails', function(){
      beforeEach(async () => {
        fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, mockPayload)
        fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 404)
      })

      describe('all submit options update the state correctly', function(){
        standardTestCases.forEach(function(testCase){
          it(`${testCase.name}`, async () => {
            await store.dispatch(testCase.function(...testCase.params))

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
        })
      })
    })

    describe('each submit function treats the URL differently', function(){
      // these tests mostly duplicate the prefetch URL checks, (as a reminder that failure doesn't modify the URL in any way from success)
      beforeEach(async () => {
        fetchMock.get(`path:${BASE_URL}/collection/uuid-ABC`, 404)
        fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 404)
      })

      it(`${submitAndNavigateCase.name} updates the URL`, async () => {
        await store.dispatch(
          submitAndNavigateCase.function(...submitAndNavigateCase.params)
        )

        expect(historyPushCallCount).toEqual(1)

        expect(history_input).toEqual({
          pathname: '/collections/details/uuid-ABC',
          search: null,
        })
      })

      it(`${submitCase.name} does not change the URL`, async () => {
        await store.dispatch(submitCase.function(...submitCase.params))

        expect(historyPushCallCount).toEqual(0)
      })
    })
  })
})
