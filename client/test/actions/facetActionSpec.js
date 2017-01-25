import '../specHelper'
import * as actions from '../../src/search/facet/FacetActions'
//import domainReducer from '../../src/reducers/domain/results'
import reducer from '../../src/reducers/reducer'
import { expect } from 'chai'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The facet action', function () {

  const initialState = reducer(undefined, {})

  it('processes new facets', function () {
    const metadata = {took: 2, total: 1, facets: {science: [{term: "Land Surface", count: 2}, {term: "Land Surface > Topography", count: 2}]}}
    const facetAction = actions.facetsReceived(metadata)
    const expectedAction = { type: 'FACETS_RECEIVED', metadata: metadata}

    facetAction.should.deep.equal(expectedAction)
  })

  it('adds facet to facets selected', function () {
    const facets = {name: "a", value: "a", selected: true}
    const expectedActions = { type: 'TOGGLE_FACET', selectedFacets: {a: ['a']}}

    const store = mockStore(initialState)
    store.dispatch(actions.toggleFacet(facets.name, facets.value, facets.selected))
    store.getActions()[0].should.deep.equal(expectedActions)
  })

  it('removes facet from facets selected', function () {
    const toggleOnAction = { type: 'TOGGLE_FACET', selectedFacets: {a: ['a']}}
    const state = reducer(initialState, toggleOnAction)
    const expectedActions = { type: 'TOGGLE_FACET', selectedFacets: {}}
    const store = mockStore(state)

    store.dispatch(actions.toggleFacet('a', 'a', false))
    store.getActions()[0].should.deep.equal(expectedActions)
  })

  it('clears facets', function () {
    const state = reducer(initialState, {})
    const expectedActions = { type: 'CLEAR_FACETS' }
    const store = mockStore(state)

    store.dispatch(actions.clearFacets())
    store.getActions()[0].should.deep.equal(expectedActions)
  })
})
