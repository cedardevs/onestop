import '../specHelper'
import * as actions from '../../src/search/facet/FacetActions'
import '../specHelper'
import * as module from '../../src/search/SearchActions'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import Immutable from 'immutable'
import nock from 'nock'
import searchQuery from '../searchQuery'
import reducer from '../../src/reducer'
import { initialState } from '../../src/search/SearchReducer'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The facet action', function () {

  const metadata = {took: 2, total: 1, facets: {science: [{term: "Land Surface", count: 2}, {term: "Land Surface > Topography", count: 2}]}}

  it('process Metadata  ', function () {
    const facetAction = actions.processMetadata(metadata);
    const expectedAction =  { type: 'METADATA_RECEIVED', metadata: metadata}

    facetAction.should.deep.equal(expectedAction)
  })

  it('update Facets Selected ', function () {
    const facet = {name: "science", value: "Atmosphere", selected: true}
    const facetAction = actions.updateFacetsSelected(facet);
    const expectedAction =   { type: 'UPDATE_FACETS', facet: facet }

    facetAction.should.deep.equal(expectedAction)
  })

  it('clear Facets ', function () {
    const facet = {name: "science", value: "Atmosphere", selected: true}
    const requestBody = JSON.stringify({queries: [{type: 'queryText', value: 'alaska'}], filters: [], facets: true})
    actions.updateFacetsSelected(facet);

    const testSearchState = initialState.mergeDeep({requestBody: requestBody})
    const initState = reducer(Immutable.Map(), {type: 'init'})
    const testState = initState.mergeDeep({search: testSearchState})

    const store = mockStore(Immutable.fromJS(testState))

    nock.disableNetConnect()
    const testingRoot = 'http://localhost:9090'
    searchQuery(testingRoot,requestBody)

    actions.clearFacets()
    return store.dispatch(module.triggerSearch(null, testingRoot))
        .then(() => {
          // TODO: Check state?
        })
  })
})
