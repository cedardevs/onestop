import '../specHelper'
import * as module from '../../src/actions/SearchRequestActions'
import { UPDATE_QUERY, updateQuery } from '../../src/actions/SearchParamActions'
import { LOADING_SHOW, LOADING_HIDE } from '../../src/actions/FlowActions'
import { SET_ERRORS } from '../../src/actions/ErrorActions'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'seamless-immutable'
import nock from 'nock'
import {searchQuery, errorQuery, errorsArray} from '../mockSearchQuery'
import {assembleSearchRequestString} from '../../src/utils/queryUtils'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The search action', () => {

  beforeEach(() => {
    nock.disableNetConnect()
  })
  afterEach(() => {
    nock.cleanAll()
  })

  const testingRoot = 'http://localhost:9090'

  it('triggerSearch executes a search from requestBody', () => {
    const testState = Immutable({
      behavior: {
        search: {
          queryText: {text: 'alaska'}
        },
        request: {collectionInFlight: false}
      },
      domain: {
        config: {
          apiHost: testingRoot
        },
        results: {
          collectionsPageOffset: 0
        }
      }
    })
    const requestBody = assembleSearchRequestString(testState, false, true)
    searchQuery(testingRoot,requestBody)

    const expectedMetadata = {"facets":{"science":[{"term":"land","count":2}]}, "total":2, "took":100}
    const expectedItems = new Map()
    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: module.FACETS_RECEIVED, metadata: expectedMetadata},
      {type: module.COUNT_HITS, totalHits: 2},
      {type: module.SEARCH_COMPLETE, items: expectedItems},
      {type: LOADING_HIDE}
    ]

    const store = mockStore(Immutable(testState))
    return store.dispatch(module.triggerSearch())
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch handles failed search requests', () => {
    const testState = Immutable({
      behavior: {
        search: {
          queryText: {text: 'alaska'}
        },
        request: {collectionInFlight: false}
      },
      domain: {
        config: {
          apiHost: testingRoot
        },
        results: {
          collectionsPageOffset: 0
        }
      }
    })
    const requestBody = assembleSearchRequestString(testState, false, true)
    errorQuery(testingRoot, requestBody)

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: LOADING_HIDE},
      {type: SET_ERRORS, errors: errorsArray},
      {type: "@@router/CALL_HISTORY_METHOD",
        payload: {
          "method": "push",
          "args": [
            "error"
          ]
        }
      },
      {type: module.CLEAR_FACETS},
      {type: module.SEARCH_COMPLETE, items: new Map()},
    ]

    const store = mockStore(testState)
    return store.dispatch(module.triggerSearch())
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch does not start a new search when a search is already in flight', () => {
    const testState = Immutable({
      behavior: {
        request: {collectionInFlight: true}
      },
      domain: {
        results: {
          collectionsPageOffset: 0
        }
      }
    })

    const store = mockStore(testState)
    return store.dispatch(module.triggerSearch())
        .then(() => {
          store.getActions().should.deep.equal([]) // No actions dispatched
        })
  })

  it('updateQuery sets searchText', () => {
    const action = updateQuery('bermuda triangle')
    const expectedAction = {type: UPDATE_QUERY, searchText: 'bermuda triangle'}

    action.should.deep.equal(expectedAction)
  })

  it('startSearch returns (like batman, but better)', () => {
    const action = module.startSearch()
    const expectedAction = {type: module.SEARCH}

    action.should.deep.equal(expectedAction)
  })

  it('completeSearch sets result items', () => {
    const items = {
      data: [
        {
          type: 'collection',
          id: 'dummyId',
          attributes: {importantInfo1: 'this is important', importantInfo2: 'but this is more important'}
        }
      ]
    }
    const action = module.completeSearch(items)
    const expectedAction = {type: module.SEARCH_COMPLETE, items: items}

    action.should.deep.equal(expectedAction)
  })
})

describe('The granule actions', function () {

  beforeEach(nock.disableNetConnect)
  afterEach(nock.cleanAll)

  const testingRoot = 'http://localhost:9090'
  const searchEndpoint = '/onestop/api/search'
  const successResponse = {
    data: [{
      type: 'granule',
      id: '1',
      attributes: {id: 1, title: 'one'},
      behavior: ""
    }, {
      type: 'granule',
      id: '2',
      attributes: {id: 2, title: 'two'},
    }],
    meta: {
      total: 42
    }
  }

  it('fetches granules with selected collections', function () {
    const collections = ['A', 'B']
    const state = {
      behavior: {
        request: {
          granuleInFlight: false
        },
        search: {
          selectedIds: collections
        }
      },
      domain: {
        config: {
          apiHost: testingRoot
        },
        results: {
          collectionsPageOffset: 0
        }
      }
    }
    const store = mockStore(Immutable(state))
    const expectedBody = assembleSearchRequestString(state, true, false)
    nock(testingRoot).post(searchEndpoint, expectedBody).reply(200, successResponse)

    return store.dispatch(module.fetchGranules()).then(() => {
      store.getActions().should.deep.equal([
        {type: LOADING_SHOW},
        {type: module.FETCHING_GRANULES},
        {type: module.COUNT_GRANULES, totalGranules: successResponse.meta.total},
        {type: module.FETCHED_GRANULES, granules: successResponse.data},
        {type: LOADING_HIDE}
      ])
    })
  })

  it('fetches granules with collection search params', function () {
    const collections = ['A', 'B']
    const state = {
      behavior: {
        request: {
          granuleInFlight: false
        },
        search: {
          selectedIds: collections,
          queryText: 'my query',
          selectedFacets: {
            location: ['Oceans']
          }
        }
      },
      domain: {
        config: {
          apiHost: testingRoot
        },
        results: {
          collectionsPageOffset: 0
        }
      }
    }
    const store = mockStore(Immutable(state))
    const expectedBody = assembleSearchRequestString(state, true, false)
    nock(testingRoot).post(searchEndpoint, expectedBody).reply(200, successResponse)

    return store.dispatch(module.fetchGranules()).then(() => {
      store.getActions().should.deep.equal([
        {type: LOADING_SHOW},
        {type: module.FETCHING_GRANULES},
        {type: module.COUNT_GRANULES, totalGranules: successResponse.meta.total},
        {type: module.FETCHED_GRANULES, granules: successResponse.data},
        {type: LOADING_HIDE}
      ])
    })
  })
})
