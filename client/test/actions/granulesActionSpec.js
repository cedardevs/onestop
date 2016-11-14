import '../specHelper'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'immutable'
import nock from 'nock'
import * as granuleActions from '../../src/result/granules/GranulesActions'
import { LOADING_SHOW, LOADING_HIDE } from '../../src/loading/LoadingActions'

const mockStore = configureMockStore([thunk])

const apiHost = 'http://localhost:9090'
const searchEndpoint = '/onestop/api/search'
const successResponse = {
  data: [{
    type: 'granule',
    id: '1',
    attributes: {id: 1, title: 'one'}
  }, {
    type: 'granule',
    id: '2',
    attributes: {id: 2, title: 'two'}
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
    const state = {apiHost: apiHost, granules: {selectedCollections: collections}}
    const store = mockStore(Immutable.fromJS(state))
    const expectedBody = JSON.stringify({filters: [{type: "collection", values: collections}], facets: false})
    nock(apiHost).post(searchEndpoint, expectedBody).reply(200, successResponse)

    return store.dispatch(granuleActions.fetchGranules()).then(() => {
      store.getActions().should.deep.equal([
        {type: LOADING_SHOW},
        {type: granuleActions.FETCHING_GRANULES},
        {type: granuleActions.FETCHED_GRANULES, granules: successResponse.data},
        {type: LOADING_HIDE},
        {payload: {args: ["granules"], method: "push"}, type: "@@router/CALL_HISTORY_METHOD"}
      ])
    })
  })

  it('fetches granules with collection search params', function () {
    const collections = ['A', 'B']
    const query = {type: "queryString", value: "my query"}
    const facetFilter = {type: "facet", name: "location", values: ["Oceans"]}
    const state = {
      apiHost: apiHost,
      search: {
        requestBody: JSON.stringify({queries: [query], filters: [facetFilter]})
      },
      granules: {
        selectedCollections: collections
      }
    }
    const store = mockStore(Immutable.fromJS(state))
    const expectedBody = JSON.stringify({
      queries: [query],
      filters: [facetFilter, {type: "collection", values: collections}],
      facets: false
    })
    nock(apiHost).post(searchEndpoint, expectedBody).reply(200, successResponse)

    return store.dispatch(granuleActions.fetchGranules()).then(() => {
      store.getActions().should.deep.equal([
        {type: LOADING_SHOW},
        {type: granuleActions.FETCHING_GRANULES},
        {type: granuleActions.FETCHED_GRANULES, granules: successResponse.data},
        {type: LOADING_HIDE},
        {payload: {args: ["granules"], method: "push"}, type: "@@router/CALL_HISTORY_METHOD"}
      ])
    })
  })
})
