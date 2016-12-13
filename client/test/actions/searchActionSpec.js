import '../specHelper'
import * as module from '../../src/search/SearchActions'
import { LOADING_SHOW, LOADING_HIDE } from '../../src/loading/LoadingActions'
import { FACETS_RECEIVED, CLEAR_FACETS } from '../../src/search/facet/FacetActions'
import { SET_ERRORS } from '../../src/error/ErrorActions'
import { initialState } from '../../src/search/SearchReducer'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'immutable'
import nock from 'nock'
import reducer from '../../src/reducer'
import {searchQuery, errorQuery, errorsArray} from '../searchQuery'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

const requestBody = JSON.stringify({queries: [{type: 'queryText', value: 'alaska'}], filters: [], facets: true})

describe('The search action', () => {

  afterEach(() => {
    nock.cleanAll()
  })

  it('triggerSearch executes a search from requestBody', () => {

    nock.disableNetConnect()

    const testingRoot = 'http://localhost:9090'
    searchQuery(testingRoot,requestBody)

    const testSearchState = initialState.mergeDeep({requestBody: requestBody})
    const initState = reducer(Immutable.Map(), {type: 'init'})
    const testState = initState.mergeDeep({search: testSearchState})

    const expectedItems = new Map()
    let expectedMetadata

    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})
    expectedMetadata = {"facets":{"science":[{"term":"land","count":2}]}, "total":2, "took":100}

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: FACETS_RECEIVED, metadata: expectedMetadata},
      {type: module.COUNT_HITS, totalHits: 2},
      {type: module.SEARCH_COMPLETE, items: expectedItems},
      {type: LOADING_HIDE},
      {type: '@@router/CALL_HISTORY_METHOD', payload: {
        args: ['results?facets=true&filters=%5B%5D&queries=%5B%7B%22type%22%3A%22queryText%22%2C%22value%22%3A%22alaska%22%7D%5D'],
        method: 'push'}
      }
    ]

    const store = mockStore(Immutable.fromJS(testState))

    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch executes a search from query params', () => {

    nock.disableNetConnect()

    const testingRoot = 'http://localhost:9090'
    searchQuery(testingRoot, requestBody)

    const initState = reducer(Immutable.Map(), {type: 'init'})

    const expectedItems = new Map()
    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})

    let expectedMetadata
    expectedMetadata = {"facets":{"science":[{"term":"land","count":2}]}, "total":2, "took":100}

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: FACETS_RECEIVED, metadata: expectedMetadata},
      {type: module.COUNT_HITS, totalHits: 2},
      {type: module.SEARCH_COMPLETE, items: expectedItems},
      {type: LOADING_HIDE},
      {type: '@@router/CALL_HISTORY_METHOD', payload: {
        args: ['results?facets=true&filters=%5B%5D&queries=%5B%7B%22type%22%3A%22queryText%22%2C%22value%22%3A%22alaska%22%7D%5D'],
        method: 'push'}
      }
    ]

    // Empty requestBody; params passed directly to triggerSearch
    const store = mockStore(Immutable.fromJS({'search':{'requestBody': requestBody}}))
    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch handles failed search requests', () => {

    nock.disableNetConnect()

    const testingRoot = 'http://localhost:9090'
    errorQuery(testingRoot, requestBody)

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: LOADING_HIDE},
      {type: SET_ERRORS, errors: errorsArray},
      {type: '@@router/CALL_HISTORY_METHOD', payload: {
        args: ['error'],
        method: 'push'}
      },
      {type: CLEAR_FACETS},
      {type: module.SEARCH_COMPLETE, items: new Map()},
    ]

    // Empty requestBody; params passed directly to triggerSearch
    const store = mockStore(Immutable.fromJS({'search':{'requestBody': requestBody}}))
    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch does not start a new search when a search is already in flight', () => {
    const testSearchState = initialState.mergeDeep({inFlight: true})
    const fullState = Immutable.fromJS({search: {}, facets: {}, results: {}, details: {}, routing: {}})
    const testState = fullState.mergeDeep({search: testSearchState})

    const store = mockStore(Immutable.fromJS(testState))
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
