import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {
  submitGranuleSearch,
  submitGranuleSearchNextPage,
  submitGranuleMatchingCount,
} from '../../../src/actions/routing/GranuleSearchRouteActions'
import {
  // used to set up pre-test conditions
  granuleNewSearchRequested,
  granuleNewSearchResultsReceived,
} from '../../../src/actions/routing/GranuleSearchStateActions'

let history_input = {}

const mockHistoryPush = input => {
  history_input = input
}
const mockHistory = {
  push: mockHistoryPush,
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
  const mockPayloadCount = {
    meta: {
      total: 13,
    },
  }

  beforeEach(async () => {
    history_input = {}
    // reset store to initial conditions
    await store.dispatch(resetStore())
  })

  afterEach(() => {
    fetchMock.reset()
  })

  describe('new search', function(){
    it('stops request if something already in flight', function(){
      //setup send something into flight first
      store.dispatch(granuleNewSearchRequested('other-uuid'))
      expect(store.getState().search.granuleRequest.inFlight).toBeTruthy()

      // then try to start a request
      store.dispatch(submitGranuleSearch(mockHistory, 'parent-uuid'))
      expect(history_input).toEqual({})
    })

    it('executes prefetch actions', function(){
      expect(store.getState().search.granuleRequest.inFlight).toBeFalsy()
      store.dispatch(submitGranuleSearch(mockHistory, 'parent-uuid'))
      expect(history_input).toEqual({
        pathname: '/collections/granules/parent-uuid',
        search: '',
      })
      expect(store.getState().search.granuleRequest.inFlight).toBeTruthy()
    })

    it('success path', async () => {
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockPayload
      )

      await store.dispatch(submitGranuleSearch(mockHistory, 'parent-uuid'))
      expect(history_input).toEqual({
        pathname: '/collections/granules/parent-uuid',
        search: '',
      })
      const {granuleRequest, granuleResult} = store.getState().search

      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual('')
      expect(granuleResult.granules).toEqual({
        'uuid-ABC': {title: 'ABC'},
        'uuid-123': {title: '123'},
      })
      expect(granuleResult.facets).toEqual(mockFacets)
      expect(granuleResult.totalGranuleCount).toEqual(10)
      expect(granuleResult.loadedGranuleCount).toEqual(2)
    })

    it('failure path', async () => {
      fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 500)

      await store.dispatch(submitGranuleSearch(mockHistory, 'parent-uuid'))
      const {granuleRequest, granuleResult} = store.getState().search
      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual(
        new Error('Internal Server Error')
      )
      expect(granuleResult.granules).toEqual({})
      expect(granuleResult.totalGranuleCount).toEqual(0)
      expect(granuleResult.loadedGranuleCount).toEqual(0)
    })

    // it('no query or filters provided is no-op', async () => { TODO
    //   store.dispatch(granuleUpdateQueryText(''))
    //   fetchMock.post(
    //     (url, opts) => url == `${BASE_URL}/search/granule`,
    //     mockPayload
    //   )
    //
    //   await store.dispatch(submitGranuleSearch(mockHistory, 'parent-uuid'))
    //   expect(history_input).toEqual({}) // The history push step skips things when no query/filter provided (as it should)
    //   const {granuleRequest, granuleResult} = store.getState().search
    //
    //   expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    //   expect(granuleRequest.errorMessage).toEqual('Invalid Request')
    // })
  })

  describe('next page', function(){
    beforeEach(async () => {
      history_input = {}
      // reset store to initial conditions
      // make sure selectedIds is set correctly
      await store.dispatch(granuleNewSearchRequested('parent-uuid'))
      expect(store.getState().search.granuleFilter.selectedIds).toEqual([
        'parent-uuid',
      ])
      // set up the 'first page' results
      await store.dispatch(
        granuleNewSearchResultsReceived(
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
    })

    it('stops request if something already in flight', function(){
      //setup send something into flight first
      store.dispatch(granuleNewSearchRequested('other-uuid'))
      expect(store.getState().search.granuleRequest.inFlight).toBeTruthy()

      // then try to start a request
      store.dispatch(submitGranuleSearchNextPage())
      expect(store.getState().search.granuleFilter.pageOffset).toBe(0)
    })

    it('executes prefetch actions', function(){
      expect(store.getState().search.granuleRequest.inFlight).toBeFalsy()
      store.dispatch(submitGranuleSearchNextPage())
      expect(history_input).toEqual({}) // it does not do history.push
      expect(store.getState().search.granuleFilter.pageOffset).toBe(20)
      expect(store.getState().search.granuleRequest.inFlight).toBeTruthy()
      expect(store.getState().search.granuleFilter.selectedIds).toEqual([
        'parent-uuid',
      ])
    })

    it('success path', async () => {
      fetchMock.post(
        (url, opts) => url == `${BASE_URL}/search/granule`,
        mockNextPagePayload
      )

      await store.dispatch(submitGranuleSearchNextPage())

      const {
        granuleRequest,
        granuleResult,
        granuleFilter,
      } = store.getState().search

      expect(granuleFilter.pageOffset).toBe(20)
      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual('')
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

    it('failure path', async () => {
      fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 500)

      await store.dispatch(submitGranuleSearchNextPage())
      const {granuleRequest} = store.getState().search
      expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
      expect(granuleRequest.errorMessage).toEqual(
        new Error('Internal Server Error')
      )
    })

    // it('no query or filters provided is no-op', async () => { TODO
    //   store.dispatch(collectionUpdateQueryText(''))
    //   fetchMock.post(
    //     (url, opts) => url == `${BASE_URL}/search/granule`,
    //     mockNextPagePayload
    //   )
    //
    //   await store.dispatch(submitGranuleSearchNextPage())
    //   const {granuleRequest, granuleResult, granuleFilter} = store.getState().search
    //
    // expect(granuleFilter.pageOffset).toBe(20) // the request was started normally
    //   expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
    //   expect(granuleRequest.errorMessage).toEqual('Invalid Request')
    //   expect(granuleResult.granules).toEqual({})
    //   expect(granuleResult.loadedGranuleCount).toEqual(0)
    // })
  })

  // describe('count only', function(){
  //   it('stops request if something already in flight', function(){
  //     //setup send something into flight first
  //     store.dispatch(granuleNewSearchRequested('other-uuid'))
  //     expect(store.getState().search.granuleRequest.inFlight).toBeTruthy()
  //
  //     // then try to start a request
  //     store.dispatch(submitGranuleMatchingCount(mockHistory, 'parent-uuid'))
  //     expect(store.getState().search.granuleResult.totalGranuleCount).toEqual(0)
  //   })
  //
  //   it('executes prefetch actions', function(){
  //     expect(store.getState().search.granuleRequest.inFlight).toBeFalsy()
  //     store.dispatch(submitGranuleMatchingCount(mockHistory, 'parent-uuid'))
  //     expect(history_input).toEqual({}) // it does not do history.push
  //     expect(store.getState().search.granuleRequest.inFlight).toBeTruthy()
  //     expect(store.getState().search.granuleFilter.selectedIds).toEqual([
  //       'parent-uuid',
  //     ])
  //   })
  //
  //   it('success path', async () => {
  //     fetchMock.post(
  //       (url, opts) => url == `${BASE_URL}/search/granule`,
  //       mockPayloadCount
  //     )
  //
  //     await store.dispatch(
  //       submitGranuleMatchingCount(mockHistory, 'parent-uuid')
  //     )
  //     const {granuleRequest, granuleResult} = store.getState().search
  //
  //     expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
  //     expect(granuleRequest.errorMessage).toEqual('')
  //     expect(granuleResult.granules).toEqual({})
  //     expect(granuleResult.facets).toEqual({})
  //     expect(granuleResult.totalGranuleCount).toEqual(13)
  //     expect(granuleResult.loadedGranuleCount).toEqual(0)
  //   })
  //
  //   it('failure path', async () => {
  //     fetchMock.post((url, opts) => url == `${BASE_URL}/search/granule`, 500)
  //
  //     await store.dispatch(
  //       submitGranuleMatchingCount(mockHistory, 'parent-uuid')
  //     )
  //     const {granuleRequest, granuleResult} = store.getState().search
  //     expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
  //     expect(granuleRequest.errorMessage).toEqual(
  //       new Error('Internal Server Error')
  //     )
  //     expect(granuleResult.granules).toEqual({})
  //     expect(granuleResult.totalGranuleCount).toEqual(0)
  //   })
  //
  //   // it('no query or filters provided is no-op', async () => { TODO
  //   //   store.dispatch(granuleUpdateQueryText(''))
  //   //   fetchMock.post(
  //   //     (url, opts) => url == `${BASE_URL}/search/granule`,
  //   //     mockPayload
  //   //   )
  //   //
  //   //   await store.dispatch(submitGranuleMatchingCount(mockHistory, 'parent-uuid'))
  //   //   expect(history_input).toEqual({}) // The history push step skips things when no query/filter provided (as it should)
  //   //   const {granuleRequest, granuleResult} = store.getState().search
  //   //
  //   //   expect(granuleRequest.inFlight).toBeFalsy() // after completing the request, inFlight is reset
  //   //   expect(granuleRequest.errorMessage).toEqual('Invalid Request')
  //   // })
  // })
})
