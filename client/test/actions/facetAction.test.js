import {
  COLLECTION_METADATA_RECEIVED,
  collectionMetadataReceived,
} from '../../src/actions/search/CollectionResultActions'
import reducer from '../../src/reducer'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import {
  COLLECTION_CLEAR_FACETS,
  collectionClearFacets,
} from '../../src/actions/search/CollectionFilterActions'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The facet action', function(){
  const initialState = reducer(undefined, {})

  it('processes new facets', function(){
    const metadata = {
      took: 2,
      total: 1,
      facets: {
        science: [
          {term: 'Land Surface', count: 2},
          {term: 'Land Surface > Topography', count: 2},
        ],
      },
    }
    const facetAction = collectionMetadataReceived(metadata)
    const expectedAction = {
      type: COLLECTION_METADATA_RECEIVED,
      metadata: metadata,
    }

    expect(facetAction).toEqual(expectedAction)
  })

  it('clears facets', function(){
    const state = reducer(initialState, {})
    const expectedActions = {type: COLLECTION_CLEAR_FACETS}
    const store = mockStore(state)

    store.dispatch(collectionClearFacets())
    expect(store.getActions()[0]).toEqual(expectedActions)
  })
})
