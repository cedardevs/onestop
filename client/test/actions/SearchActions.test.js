import fetchMock from 'fetch-mock'
import React from 'react'
import {mount} from 'enzyme'
import App from '../../src/App'
import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'

import {
  collectionErrorsArray,
  mockSearchCollectionResponse,
  mockSearchCollectionErrorResponse,
} from '../mocks/mockSearchCollection'

// TODO collapse the two CollectionSearchStateActions ...
import * as CollectionFilterActions from '../../src/actions/routing/CollectionSearchStateActions'
import * as CollectionRequestActions from '../../src/actions/routing/CollectionSearchStateActions'
// import * as SearchActions from '../../src/actions/search/SearchActions'
import * as CollectionSearchActions from '../../src/actions/routing/CollectionSearchRouteActions'

import {RESET_STORE} from '../../src/reducer'
import {mockSearchGranuleResponse} from '../mocks/mockSearchGranule'
import {apiPath} from '../../src/utils/urlUtils'
import {mockConfigResponse} from '../mocks/mockConfig'
import {mockInfoResponse} from '../mocks/mockInfo'
import {
  mockCollectionCountResponse,
  mockGranuleCountResponse,
} from '../mocks/mockCount'

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
}

const mockHistoryPush = () => {} // TODO using the actual history object is causing an error (that is sufficiently buried I'm not sure yet what it is!)
const mockHistory = {
  push: mockHistoryPush,
}


// describe('The granule actions', () => {
//   let url = '/'
//   let urlSearchGranule = '/-search/search/granule'
//   let component = null
//   let stateBefore = null
//   const resetStore = () => ({type: RESET_STORE})
//
//   beforeAll(() => {
//     // initially go to index/home
//     history.push(url)
//     // mount the entire application with store and history
//     // tests use memoryHistory based on NODE_ENV=='test'
//     component = mount(App(store, history))
//   })
//
//   beforeEach(async () => {
//     // return to index/home
//     history.push(url)
//     // reset store to initial conditions
//     await store.dispatch(resetStore())
//     // capture state before test
//     stateBefore = store.getState()
//   })
//
//   afterEach(() => {
//     fetchMock.reset()
//   })
//
//   it('fetches granules with selected collections', async () => {
//     // mock search request & response
//     fetchMock.post(`path:${urlSearchGranule}`, mockSearchGranuleResponse)
//
//     // update selected collection ids via `collectionToggleSelectedId` action
//     const collectionIds = [ 'A', 'B' ]
//     await Promise.all(
//       collectionIds.map(collectionId => {
//         return store.dispatch(
//           CollectionFilterActions.collectionToggleSelectedId(collectionId)
//         )
//       })
//     )
//
//     // trigger search
//     await store.dispatch(SearchActions.fetchGranules())
//
//     const actualGranules = store.getState().search.collectionResult.granules
//     const expectedGranules = {
//       '1': {
//         id: 1,
//         title: 'one',
//       },
//       '2': {
//         id: 2,
//         title: 'two',
//       },
//     }
//     expect(actualGranules).toEqual(expectedGranules)
//   })
//
//   it('fetches granules with selected collections, queryText, and selectedFacets', async () => {
//     // mock search request & response
//     fetchMock.post(`path:${urlSearchGranule}`, mockSearchGranuleResponse)
//
//     // update selected collection ids via `collectionToggleSelectedId` action
//     const collectionIds = [ 'A', 'B' ]
//     await Promise.all(
//       collectionIds.map(collectionId => {
//         return store.dispatch(
//           CollectionFilterActions.collectionToggleSelectedId(collectionId)
//         )
//       })
//     )
//
//     // update search query via redux store action
//     const newQueryText = 'bermuda triangle'
//     await store.dispatch(
//       CollectionFilterActions.collectionUpdateQueryText(newQueryText)
//     )
//
//     // updated selected facets via `collectionToggleFacet` action
//     const selectedFacets = [
//       {
//         category: 'science',
//         facetName: 'Agriculture',
//         selected: true,
//       },
//     ]
//     await Promise.all(
//       selectedFacets.map(facet => {
//         return store.dispatch(
//           CollectionFilterActions.collectionToggleFacet(
//             facet.category,
//             facet.facetName,
//             facet.selected
//           )
//         )
//       })
//     )
//
//     // trigger search
//     await store.dispatch(SearchActions.fetchGranules())
//
//     const actualGranules = store.getState().search.collectionResult.granules
//     const expectedGranules = {
//       '1': {
//         id: 1,
//         title: 'one',
//       },
//       '2': {
//         id: 2,
//         title: 'two',
//       },
//     }
//     const actualQueryText = store.getState().search.collectionFilter.queryText
//     const actualSelectedFacets = store.getState().search.collectionFilter
//       .selectedFacets
//     const actualNumScienceFacets = actualSelectedFacets['science'].length
//     const expectedNumScienceFacets = selectedFacets.filter(facet => {
//       return facet.category === 'science'
//     }).length
//
//     expect(actualGranules).toEqual(expectedGranules)
//     expect(actualQueryText).toBe(newQueryText)
//     expect(actualNumScienceFacets).toBe(expectedNumScienceFacets)
//   })
// })
//
// describe('The flow actions', () => {
//   let url = '/'
//   let component = null
//   let stateBefore = null
//   const resetStore = () => ({type: RESET_STORE})
//
//   beforeAll(() => {
//     // initially go to index/home
//     history.push(url)
//     // mount the entire application with store and history
//     // tests use memoryHistory based on NODE_ENV=='test'
//     component = mount(App(store, history))
//   })
//
//   beforeEach(async () => {
//     // return to index/home
//     history.push(url)
//     // reset store to initial conditions
//     await store.dispatch(resetStore())
//     // capture state before test
//     stateBefore = store.getState()
//   })
//
//   afterEach(() => {
//     fetchMock.reset()
//   })
//
//   it('initialize triggers config, version info, total counts, and data loading', async () => {
//     // mock fetch config
//     fetchMock.get(`path:${apiPath()}/uiConfig`, mockConfigResponse)
//
//     // mock fetch info
//     fetchMock.get(`path:${apiPath()}/actuator/info`, mockInfoResponse)
//
//     // mock fetch collection & granule counts
//     fetchMock.get(`path:${apiPath()}/collection`, mockCollectionCountResponse)
//     fetchMock.get(`path:${apiPath()}/granule`, mockGranuleCountResponse)
//
//     // debugStore("BEFORE")
//
//     // trigger initialize
//     // store.dispatch(SearchActions.initialize()).then(() => {
//     //   debugStore("THEN1")
//     // })
//
//     // debugStore("AFTER")
//     ///
//
//     // TODO: is there a dangling event handler here?
//     // why do we have to use `mocha --exit` in this test for mocha to exit properly?
//     // we'll keep this commented out until we consider changes to initialize()
//   })
//
//   // TODO: rewrite these tests with new testing paradigm
//   // describe('loadData', function () {
//   //   it('loads only collections if no ids are selected', function () {
//   //     const getState = sinon.stub().returns(mockDefaultState)
//   //     const fn = actions.loadCollections()
//   //
//   //     fn(dispatch, getState)
//   //     const dispatchCalls = dispatch.callCount
//   //     assert(dispatchCalls === 1, `There were ${dispatchCalls} dispatch calls made`)
//   //   })
//   //
//   //   it('loads collections and granules if ids are selected', function () {
//   //     const stateWithIds = _.merge(mockDefaultState, {behavior: {search: {selectedIds: [123]}}})
//   //     const getState = sinon.stub().returns(stateWithIds)
//   //     const fn = actions.loadCollections()
//   //
//   //     fn(dispatch, getState)
//   //     const dispatchCalls = dispatch.callCount
//   //     assert(dispatchCalls === 2, `There were ${dispatchCalls} dispatch calls made`)
//   //   })
//   // })
//
//   // it('do not dispatch a transition to the collections view, just a collectionClearSelectedIds action, when no search params are present', function () {
//   //   const getState = sinon.stub().returns(mockDefaultState)
//   //   const fn = actions.showCollections()
//   //
//   //   fn(dispatch, getState)
//   //   expect(dispatch.callCount).toBe(1)
//   // })
//   //
//   // it('dispatch a collectionClearSelectedIds action and transition to the collections view when search params are present', function () {
//   //   const stateWithSearchParams = _.merge(mockDefaultState, {behavior: {search: {queryText: 'oceans'}}})
//   //   const getState = sinon.stub().returns(stateWithSearchParams)
//   //   const fn = actions.showCollections()
//   //
//   //   fn(dispatch, getState)
//   //   expect(dispatch.callCount).toBe(2)
//   // })
// })
