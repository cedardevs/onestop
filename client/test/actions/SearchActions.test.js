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

import * as CollectionFilterActions from '../../src/actions/search/CollectionFilterActions'
import * as CollectionRequestActions from '../../src/actions/search/CollectionRequestActions'
// import * as SearchActions from '../../src/actions/search/SearchActions'
import * as CollectionSearchActions from '../../src/actions/search/CollectionSearchActions'

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

describe('The search action', () => {
  let url = '/'
  let urlSearchCollection = '/-search/search/collection'
  let component = null
  let stateBefore = null
  const resetStore = () => ({type: RESET_STORE})

  beforeAll(() => {
    // initially go to index/home
    history.push(url)
    // mount the entire application with store and history
    // tests use memoryHistory based on NODE_ENV=='test'
    component = mount(App(store, history))
  })

  beforeEach(async () => {
    // return to index/home
    history.push(url)
    // reset store to initial conditions
    await store.dispatch(resetStore())
    // capture state before test
    stateBefore = store.getState()
  })

  afterEach(() => {
    fetchMock.reset()
  })

  it('new collection search executes when a query is set and updates collections and facets', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, mockSearchCollectionResponse)

    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText('alaska')
    )

    // trigger search
    await store.dispatch(
      CollectionSearchActions.asyncNewCollectionSearch(mockHistory)
    )

    const actualCollections = store.getState().search.collectionResult
      .collections
    const expectedCollections = {
      '123ABC': {
        field0: 'field0',
        field1: 'field1',
      },
      '789XYZ': {
        field0: 'field00',
        field1: 'field01',
      },
    }
    const actualFacets = store.getState().search.collectionResult.facets
    const expectedFacets = {
      science: [
        {
          term: 'land',
          count: 2,
        },
      ],
    }
    expect(actualCollections).toEqual(expectedCollections)
    expect(actualFacets).toEqual(expectedFacets)
  })

  it('new collection search does not do anything when a query is not set', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, mockSearchCollectionResponse)

    // ...omit setting query via `collectionUpdateQueryText` action

    // trigger search
    await store.dispatch(
      CollectionSearchActions.asyncNewCollectionSearch(mockHistory)
    )

    const actualCollections = store.getState().search.collectionResult
      .collections
    const expectedCollections = {}
    const actualFacets = store.getState().search.collectionResult.facets
    const expectedFacets = {}
    expect(actualCollections).toEqual(expectedCollections)
    expect(actualFacets).toEqual(expectedFacets)
  })

  it('new collection search handles failed search requests', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, {
      status: 500,
      body: mockSearchCollectionErrorResponse,
    })

    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText('alaska')
    )

    // trigger search
    await store.dispatch(
      CollectionSearchActions.asyncNewCollectionSearch(mockHistory)
    )

    const actualErrors = store.getState().errors
    const expectedErrors = collectionErrorsArray

    expect(actualErrors).toEqual(expectedErrors)
  })

  it('new search does not start a new search when a search is already in flight', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, mockSearchCollectionResponse)

    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText('alaska')
    )

    // the `collectionSearchRequest` action is what triggers the `inFlight` to true
    // we want to artificially set this after we set a valid query so that we can ensure no results come back
    // in other words, the fetch mocked above should never trigger when we know another search is running
    await store.dispatch(
      CollectionRequestActions.collectionNewSearchRequested()
    )

    // trigger search
    store.dispatch(
      CollectionSearchActions.asyncNewCollectionSearch(mockHistory)
    )

    const actualCollections = store.getState().search.collectionResult
      .collections
    const expectedCollections = {}
    const actualFacets = store.getState().search.collectionResult.facets
    const expectedFacets = {}
    expect(actualCollections).toEqual(expectedCollections)
    expect(actualFacets).toEqual(expectedFacets)
  })

  it('collectionUpdateQueryText sets queryText', async () => {
    // TODO how does this meaningfully differ from the reducer tests?
    const queryTextBefore = stateBefore.search.collectionFilter.queryText
    const newQueryText = 'bermuda triangle'
    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText(newQueryText)
    )
    const queryTextAfter = store.getState().search.collectionFilter.queryText
    expect(queryTextAfter).not.toBe(queryTextBefore)
    expect(queryTextAfter).toBe(newQueryText)
  })

  it('collectionSearchRequest sets inFlight', async () => {
    // TODO how does this meaningfully differ from the reducer tests?
    const collectionInFlightBefore =
      stateBefore.search.collectionRequest.inFlight

    await store.dispatch(
      CollectionRequestActions.collectionNewSearchRequested()
    )
    const collectionInFlightAfter = store.getState().search.collectionRequest
      .inFlight
    expect(collectionInFlightBefore).not.toBeTruthy()
    expect(collectionInFlightAfter).toBeTruthy()
  })

  it('collectionSearchSuccess sets result items and resets inFlight to false', async () => {
    // TODO how does this meaningfully differ from the reducer tests?
    const collectionsBefore = stateBefore.search.collectionResult.collections

    await store.dispatch(
      CollectionRequestActions.collectionNewSearchRequested()
    )
    // the mock items to "complete" the search with
    const items = [
      {
        id: 'data1',
        type: 'collection',
        attributes: {
          importantInfo1: 'this is important',
          importantInfo2: 'but this is more important',
        },
      },
      {
        id: 'data2',
        type: 'collection',
        attributes: {
          importantInfo3: 'what could possibly be this important?',
          importantInfo4: 'how about this!',
        },
      },
    ]

    await store.dispatch(
      CollectionRequestActions.collectionNewSearchResultsRecieved(2, items, {})
    )
    const collectionsAfter = store.getState().search.collectionResult
      .collections
    const collectionInFlightAfter = store.getState().search.collectionRequest
      .inFlight

    const expectedCollectionKeys = [ 'data1', 'data2' ]
    const actualCollectionsKeys = Object.keys(collectionsAfter)

    expect(collectionsBefore).toEqual({})
    expect(actualCollectionsKeys).toEqual(expectedCollectionKeys)
    expect(collectionInFlightAfter).not.toBeTruthy()
  })
})

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
