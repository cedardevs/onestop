// import configureMockStore from 'redux-mock-store'
// import thunk from 'redux-thunk'
// import reducer from '../../src/reducer'
// import * as CollectionFilterActions from '../../src/actions/search/CollectionFilterActions'
// import {
//   COLLECTION_TOGGLE_FACET,
// } from '../../src/actions/search/CollectionFilterActions'
//
// describe('The search params actions', function(){
//
//   describe('for facets', function(){
//     const middlewares = [ thunk ]
//     const mockStore = configureMockStore(middlewares)
//     const initialState = reducer(undefined, {})
//
//     it('adds facet to facets selected', function(){
//       const facets = {name: 'a', value: 'a', selected: true}
//       const expectedActions = {
//         type: COLLECTION_TOGGLE_FACET,
//         selectedFacets: {a: [ 'a' ]},
//       }
//
//       const store = mockStore(initialState)
//       store.dispatch(
//         CollectionFilterActions.collectionToggleFacet(
//           facets.name,
//           facets.value,
//           facets.selected
//         )
//       )
//       expect(store.getActions()[0]).toEqual(expectedActions)
//     })
//
//     it('removes facet from facets selected', function(){
//       const toggleOnAction = {
//         type: COLLECTION_TOGGLE_FACET,
//         selectedFacets: {a: [ 'a' ]},
//       }
//       const state = reducer(initialState, toggleOnAction)
//       const expectedActions = {
//         type: COLLECTION_TOGGLE_FACET,
//         selectedFacets: {},
//       }
//       const store = mockStore(state)
//
//       store.dispatch(
//         CollectionFilterActions.collectionToggleFacet('a', 'a', false)
//       )
//       expect(store.getActions()[0]).toEqual(expectedActions)
//     })
//   })
// })
