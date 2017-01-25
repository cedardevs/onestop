import '../specHelper'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'seamless-immutable'
import nock from 'nock'
import * as granuleActions from '../../src/result/granules/GranulesActions'
import { LOADING_SHOW, LOADING_HIDE } from '../../src/loading/LoadingActions'
import { assembleSearchRequestString } from '../../src/utils/queryUtils'

const mockStore = configureMockStore([thunk])

const apiHost = 'http://localhost:9090'
const searchEndpoint = '/onestop/api/search'
const successResponse = {
  data: [{
    type: 'granule',
    id: '1',
    attributes: {id: 1, title: 'one'},
    appState: ""
  }, {
    type: 'granule',
    id: '2',
    attributes: {id: 2, title: 'two'},
  }],
  meta: {}
}
const errorResponse = {
  errors: [{
    status: '500',
    title: 'Sorry, something has gone wrong',
    detail: 'Looks like something isn\'t working on our end, please try again later',
  }],
  meta: {
    timestamp: new Date().time,
    request: 'uri:/onestop/api/search'
  }
}


describe('The granule actions', function () {

  beforeEach(nock.disableNetConnect)
  afterEach(nock.cleanAll)

  it('fetches granules with selected collections', function () {
    const collections = ['A', 'B']
    // const state = {apiHost: apiHost, collections: {selectedIds: collections},
    //   granules: {inFlight: false}, search: {requestBody: '{}'}}
    const state = {
      apiHost: apiHost,
      appState: {
        granuleRequest: {
          inFlight: false
        },
        search: {
          selectedIds: collections
        }
      }
    }
    const store = mockStore(Immutable(state))
    const expectedBody = assembleSearchRequestString(state, true)
    nock(apiHost).post(searchEndpoint, expectedBody).reply(200, successResponse)

    return store.dispatch(granuleActions.fetchGranules()).then(() => {
      store.getActions().should.deep.equal([
        {type: LOADING_SHOW},
        {type: granuleActions.FETCHING_GRANULES},
        {type: granuleActions.FETCHED_GRANULES, granules: successResponse.data,
          view: 'collections/files'},
        {type: LOADING_HIDE}
      ])
    })
  })

  it('fetches granules with collection search params', function () {
    const collections = ['A', 'B']
    const state = {
      apiHost: apiHost,
      appState: {
        granuleRequest: {
          inFlight: false
        },
        search: {
          selectedIds: collections,
          queryText: 'my query',
          selectedFacets: {
            location: ['Oceans']
          }
        }
      }
    }
    const store = mockStore(Immutable(state))
    const expectedBody = assembleSearchRequestString(state, true)
    nock(apiHost).post(searchEndpoint, expectedBody).reply(200, successResponse)

    return store.dispatch(granuleActions.fetchGranules()).then(() => {
      store.getActions().should.deep.equal([
        {type: LOADING_SHOW},
        {type: granuleActions.FETCHING_GRANULES},
        {type: granuleActions.FETCHED_GRANULES, granules: successResponse.data,
          view: 'collections/files'},
        {type: LOADING_HIDE}
      ])
    })
  })
})
