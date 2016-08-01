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

    const testingRoot = 'http://localhost:8080'
    const requestBody = JSON.stringify({queries: [{type: 'queryText', value: 'alaska'}], filters: []})

    nock(testingRoot)
        .filteringPath(function(path){
          return '/';
        })
        .log(console.log)
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
    const testState = initialState.mergeDeep({requestBody: requestBody})

    const expectedItems = {
      "123ABC": {type: 'collection', field0: 'field0', field1: 'field1'},
      "789XYZ": {type: 'collection', field0: 'field00', field1: 'field01'}
    }

    const expectedActions = [
      {type: module.SEARCH},
      {type: module.SEARCH_COMPLETE, items: expectedItems}
    ]

    const store = mockStore(Immutable.fromJS(testState))
    return store.dispatch(module.triggerSearch(testingRoot))
        .then(() => {
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