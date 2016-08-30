import '../specHelper'
import * as actions from '../../src/search/facet/FacetActions'

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
    const facetAction = actions.clearFacets();
    searchQuery(requestBody)


    facetAction.should.deep.equal(expectedAction)
  })
})

