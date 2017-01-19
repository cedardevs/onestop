import '../specHelper'
import * as module from '../../src/search/SearchActions'
import { LOADING_SHOW, LOADING_HIDE } from '../../src/loading/LoadingActions'
import { FACETS_RECEIVED, CLEAR_FACETS } from '../../src/search/facet/FacetActions'
import { SET_ERRORS } from '../../src/error/ErrorActions'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'seamless-immutable'
import nock from 'nock'
import reducer from '../../src/reducers/reducer'
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

    const testSearchState = Immutable({requestBody: requestBody})
    const initState = reducer(new Map(), {type: 'init'})
    const testState = Immutable.merge(initState, {search: testSearchState})

    const expectedItems = new Map()
    let expectedFacets

    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})
    expectedFacets = {"facets":{"science":[{"term":"land","count":2}]}}

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: FACETS_RECEIVED, metadata: expectedFacets},
      {type: module.SEARCH_COMPLETE, items: expectedItems,
          view: 'collections', appState: ''},
      {type: LOADING_HIDE}
    ]

    const store = mockStore(Immutable(testState))

    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch executes a search from query params', () => {

    nock.disableNetConnect()

    const testingRoot = 'http://localhost:9090'
    searchQuery(testingRoot, requestBody)

    const expectedItems = new Map()
    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})

    let expectedFacets
    expectedFacets = {facets: {science: [{term: "land", count: 2}]}}

    const expectedActions = [
      {type: LOADING_SHOW},
      {type: module.SEARCH},
      {type: FACETS_RECEIVED, metadata: expectedFacets},
      {type: module.SEARCH_COMPLETE, items: expectedItems,
          view: 'collections'},
      {type: LOADING_HIDE}
    ]

    // Empty requestBody; params passed directly to triggerSearch
    const store = mockStore(Immutable({'search':{'requestBody': requestBody}}))
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
      {type: "@@router/CALL_HISTORY_METHOD",
        payload: {
          "method": "push",
          "args": [
            "error"
          ]
        }
      },
      {type: CLEAR_FACETS},
      {type: module.SEARCH_COMPLETE, items: new Map(),
          view: 'collections'},
    ]

    // Empty requestBody; params passed directly to triggerSearch
    const store = mockStore(Immutable({'search':{'requestBody': requestBody}}))
    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it('triggerSearch does not start a new search when a search is already in flight', () => {
    const testSearchState = Immutable({inFlight: true})
    const fullState = Immutable({search: {}, facets: {}, results: {}, details: {}, routing: {}})
    const testState = Immutable({search: testSearchState})

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
    const expectedAction = {type: module.SEARCH_COMPLETE, items: items,
          view: 'collections'}

    action.should.deep.equal(expectedAction)
  })
})
