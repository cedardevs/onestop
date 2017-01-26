import '../specHelper'
import * as module from '../../src/search/SearchActions'
import { LOADING_SHOW, LOADING_HIDE } from '../../src/loading/LoadingActions'
import { FACETS_RECEIVED, CLEAR_FACETS } from '../../src/search/facet/FacetActions'
import { SET_ERRORS } from '../../src/error/ErrorActions'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'seamless-immutable'
import nock from 'nock'
import {searchQuery, errorQuery, errorsArray} from '../searchQuery'
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

  it('triggerSearch executes a search from requestBody', () => {
    const testState = Immutable({
      behavior: {
        search: {
          queryText: {text: 'alaska'}
        },
        request: {collectionInFlight: false}
      }
    })
    const testingRoot = 'http://localhost:9090'
    const requestBody = assembleSearchRequestString(testState)
    searchQuery(testingRoot,requestBody)

    const expectedMetadata = {"facets":{"science":[{"term":"land","count":2}]}, "total":2, "took":100}
    const expectedItems = new Map()
    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: FACETS_RECEIVED, metadata: expectedMetadata},
      {type: module.COUNT_HITS, totalHits: 2},
      {type: module.SEARCH_COMPLETE, items: expectedItems},
      {type: LOADING_HIDE}
    ]

    const store = mockStore(Immutable(testState))
    return store.dispatch(module.triggerSearch(testingRoot))
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
      }
    })
    const testingRoot = 'http://localhost:9090'
    const requestBody = assembleSearchRequestString(testState)
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
      {type: CLEAR_FACETS},
      {type: module.SEARCH_COMPLETE, items: new Map()},
    ]

    const store = mockStore(testState)
    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch does not start a new search when a search is already in flight', () => {
    const testState = Immutable({
      behavior: {
        request: {collectionInFlight: true}
      }
    })

    const store = mockStore(testState)
    return store.dispatch(module.triggerSearch())
        .then(() => {
          store.getActions().should.deep.equal([]) // No actions dispatched
        })
  })

  it('updateQuery sets searchText', () => {
    const action = module.updateQuery('bermuda triangle')
    const expectedAction = {type: module.UPDATE_QUERY, searchText: 'bermuda triangle'}

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
