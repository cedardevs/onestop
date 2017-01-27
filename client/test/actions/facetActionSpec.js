import '../specHelper'
import { facetsReceived, clearFacets } from '../../src/actions/SearchRequestActions'
import reducer from '../../src/reducers/reducer'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The facet action', function () {

  const initialState = reducer(undefined, {})

  it('processes new facets', function () {
    const metadata = {took: 2, total: 1, facets: {science: [{term: "Land Surface", count: 2}, {term: "Land Surface > Topography", count: 2}]}}
    const facetAction = facetsReceived(metadata)
    const expectedAction = { type: 'FACETS_RECEIVED', metadata: metadata}

    facetAction.should.deep.equal(expectedAction)
  })

  it('clears facets', function () {
    const state = reducer(initialState, {})
    const expectedActions = { type: 'CLEAR_FACETS' }
    const store = mockStore(state)

    store.dispatch(clearFacets())
    store.getActions()[0].should.deep.equal(expectedActions)
  })
})
