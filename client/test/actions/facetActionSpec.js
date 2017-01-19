import '../specHelper'
import * as actions from '../../src/search/facet/FacetActions'
import reducer from '../../src/reducers/reducer'
import { expect } from 'chai'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The facet action', function () {

  const metadata = {took: 2, total: 1, facets: {science: [{term: "Land Surface", count: 2}, {term: "Land Surface > Topography", count: 2}]}}
  const inititalState = reducer(undefined, {})

  it('process new facets', function () {
    const facetAction = actions.facetsReceived(metadata)
    const expectedAction = { type: 'FACETS_RECEIVED', metadata: metadata}

    facetAction.should.deep.equal(expectedAction)
  })

  it('add facet to facets selected', function () {
    const facets = {name: "a", value: "a", selected: true}
    const expectedActions = { type: 'TOGGLE_FACET', selectedFacets: {a: ['a']}}
    const state = Object.assign(inititalState, { facets: {selectedFacets: {} }})
    const store = mockStore(state)
    store.dispatch(
      actions.toggleFacet(facets.name, facets.value, facets.selected))
    store.getActions()[0].should.deep.equal(expectedActions)
  })

  it('remove facet from facets selected', function () {
    const facets = {name: "a", value: "a", selected: false}
    const expectedActions = { type: 'TOGGLE_FACET', selectedFacets: {}}

    const state = Object.assign(inititalState, { facets: {selectedFacets: {a:['a']} }})
    const store = mockStore(state)
    store.dispatch(
      actions.toggleFacet(facets.name, facets.value, facets.selected))
    store.getActions()[0].should.deep.equal(expectedActions)
  })

  it('clear facets', function () {
    const expectedActions = { type: 'CLEAR_FACETS' }
    const state = Object.assign(inititalState, { facets: {selectedFacets: {} }})
    const store = mockStore(state)
    store.dispatch(
      actions.clearFacets())
      store.getActions()[0].should.deep.equal(expectedActions)
    })
  })
