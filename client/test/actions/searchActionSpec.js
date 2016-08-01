import '../specHelper'
import * as module from '../../src/search/SearchActions'
import { initialState } from '../../src/search/SearchReducer'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'immutable'
import nock from 'nock'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The search action', () => {

  afterEach(() => {
    nock.cleanAll()
  })

  it('triggerSearch handles a new search', () => {

    nock.disableNetConnect()

    const testingRoot = 'http://localhost:9090'
    const requestBody = {queries: [{type: 'queryText', value: 'alaska'}], filters: []}

    nock(testingRoot)
/*        .filteringPath(function(path){
          return '/';
        })
        .log(console.log)*/
        .post('/api/search', requestBody)
        .reply(200, {
          data: [
            {
              type: 'collection',
              id: '123ABC',
              attributes: {
                field0: 'field0',
                field1: 'field1'
              }
            },
            {
              type: 'collection',
              id: '789XYZ',
              attributes: {
                field0: 'field00',
                field1: 'field01'
              }
            }
            ]
        })
    const testSearchState = initialState.mergeDeep({requestBody: JSON.stringify(requestBody)})
    const fullState = Immutable.fromJS({search: {}, facets: {}, results: {}, details: {}, routing: {}})
    const testState = fullState.mergeDeep({search: testSearchState})

    const expectedItems = new Map()
    expectedItems.set("123ABC", {type: 'collection', field0: 'field0', field1: 'field1'})
    expectedItems.set("789XYZ", {type: 'collection', field0: 'field00', field1: 'field01'})

    const expectedActions = [
      {type: module.SEARCH},
      {type: module.SEARCH_COMPLETE, items: expectedItems},
      {type: '@@router/CALL_HISTORY_METHOD', payload: {
        args: ['results?filters=%5B%5D&queries=%5B%7B%22type%22%3A%22queryText%22%2C%22value%22%3A%22alaska%22%7D%5D'],
        method: 'push'}
      }
    ]
    console.log(expectedActions)

    const store = mockStore(Immutable.fromJS(testState))
    return store.dispatch(module.triggerSearch(null, testingRoot))
        .then(() => {
          console.log(store.getActions())
          store.getActions().should.deep.equal(expectedActions)
        })
  })

  it.skip('triggerSearch returns promise when a search is already in flight', () => {
    // TODO
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