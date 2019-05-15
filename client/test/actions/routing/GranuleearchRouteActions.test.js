import store from '../../../src/store' // create Redux store with appropriate middleware
import {RESET_STORE} from '../../../src/reducer'
import fetchMock from 'fetch-mock'

import {submitGranuleSearch} from '../../../src/actions/routing/GranuleSearchRouteActions'
import {granuleNewSearchRequested} from '../../../src/actions/routing/GranuleSearchStateActions'

let history_input = {}

const mockHistoryPush = input => {
  history_input = input
}
const mockHistory = {
  push: mockHistoryPush,
}

describe('granule search action', function(){
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
    // reset store to initial conditions
    await store.dispatch(resetStore())
  })

  afterEach(() => {
    fetchMock.reset()
  })

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
      search: '?i=parent-uuid',
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
      search: '?i=parent-uuid',
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

  // it('no query or filters provided is no-op', async () => {
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
