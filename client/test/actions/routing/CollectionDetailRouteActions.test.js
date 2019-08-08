import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {submitCollectionDetail} from '../../../src/actions/routing/CollectionDetailRouteActions'
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
  const BASE_URL = '/onestop-search'
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

  const submitCase = {
    name: 'submit',
    function: submitCollectionDetail,
    params: [ mockHistory, 'uuid-ABC', {} ],
  }

  const standardTestCases = [ submitCase ]

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
        name: 'id null, submit',
        function: submitCollectionDetail,
        params: [ mockHistory, null, {} ],
      },
      {
        name: 'id string, submit',
        function: submitCollectionDetail,
        params: [ mockHistory, '', {} ],
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
          // backgroundInFlight state is determined by GRANULE_MATCHING_COUNT_REQUESTED action
          // which now only applies intelligently (if there are filters applied)
          // if no filters are applied, the granule count request is unnecessary
          // In other words: if this test were to simulate applied filters, this would be truthy,
          // but for now it's falsy!
          expect(collectionDetailRequest.backgroundInFlight).toBeFalsy()
          expect(collectionDetailRequest.requestedID).toEqual('uuid-ABC')
          expect(collectionDetailFilter.selectedCollectionIds).toEqual([
            'uuid-ABC',
          ])
          // TODO worth asserting other filter state, or let it be assumed by the tests directly against that reducer + action?
        })
      })
    })

    describe('submit function treats the URL correctly', function(){
      it(`${submitCase.name} updates the URL`, function(){
        store.dispatch(submitCase.function(...submitCase.params))

        expect(historyPushCallCount).toEqual(1)

        expect(history_input).toEqual({
          pathname: '/collections/details/uuid-ABC',
          search: '',
        })
      })

      it(`${submitCase.name} does not update history if no change`, function(){
        // start already at that URL
        mockHistory.location = {
          pathname: '/collections/details/uuid-ABC',
          search: '',
        }

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
  })
})
