import '../specHelper'
import * as actions from '../../src/search/facet/FacetActions'
import '../specHelper'
import { expect } from 'chai'


describe('The facet action', function () {

  const metadata = {took: 2, total: 1, facets: {science: [{term: "Land Surface", count: 2}, {term: "Land Surface > Topography", count: 2}]}}

  it('process new facets  ', function () {
    const facetAction = actions.facetsReceived(metadata)
    const expectedAction =  { type: 'FACETS_RECEIVED', metadata: metadata, processFacets: false}

    facetAction.should.deep.equal(expectedAction)
  })

  it('update facets selected ', function () {
    const facet = {name: "science", value: "Atmosphere", selected: true}
    const facetAction = actions.updateFacetsSelected(facet)
    const expectedAction =   { type: 'UPDATE_FACETS', facetsSelected: facet }

    facetAction.should.deep.equal(expectedAction)
  })

  it('clear facets ', function () {
    const facet = {name: "science", value: "Atmosphere", selected: true}
    const facetAction = actions.updateFacetsSelected(facet)
    const expectedAction = {type: 'UPDATE_FACETS', facetsSelected: facet}

    facetAction.should.deep.equal(expectedAction)

    const clearFacet = actions.clearFacets()

    expect(clearFacet).to.have.any.keys('type', 'CLEAR_FACETS')
  })
})
