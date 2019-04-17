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
import * as CollectionResultActions from '../../src/actions/search/CollectionResultActions'
import {RESET_STORE} from '../../src/reducer'
import {mockSearchGranuleResponse} from '../mocks/mockSearchGranule'
import {collectionToggleFacet} from '../../src/actions/search/CollectionFilterActions'

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
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

  it('triggerSearch executes a search when a query is set and updates collections and facets', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, mockSearchCollectionResponse)

    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText('alaska')
    )

    // trigger search and ask for facets
    const retrieveFacets = true
    await store.dispatch(SearchRequestActions.triggerSearch(retrieveFacets))

    const actualCollections = store.getState().search.collectionResult
      .collections
    const expectedCollections = {
      '123ABC': {
        type: 'collection',
        field0: 'field0',
        field1: 'field1',
      },
      '789XYZ': {
        type: 'collection',
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

  it('triggerSearch fails to updates collections and facets when a query is not set', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, mockSearchCollectionResponse)

    // ...omit setting query via `collectionUpdateQueryText` action

    // trigger search and ask for facets
    const retrieveFacets = true
    await store.dispatch(SearchRequestActions.triggerSearch(retrieveFacets))

    const actualCollections = store.getState().search.collectionResult
      .collections
    const expectedCollections = {}
    const actualFacets = store.getState().search.collectionResult.facets
    const expectedFacets = {}
    expect(actualCollections).toEqual(expectedCollections)
    expect(actualFacets).toEqual(expectedFacets)
  })

  it('triggerSearch handles failed search requests', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, {
      status: 500,
      body: mockSearchCollectionErrorResponse,
    })

    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText('alaska')
    )

    // trigger search and ask for facets
    const retrieveFacets = true
    await store.dispatch(SearchRequestActions.triggerSearch(retrieveFacets))

    const actualErrors = store.getState().behavior.errors
    const expectedErrors = collectionErrorsArray

    expect(actualErrors).toEqual(expectedErrors)
  })

  it('triggerSearch does not start a new search when a search is already in flight', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchCollection}`, mockSearchCollectionResponse)

    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText('alaska')
    )

    // the `collectionSearchRequest` action is what triggers the `collectionSearchRequestInFlight` to true
    // we want to artificially set this after we set a valid query so that we can ensure no results come back
    // in other words, the fetch mocked above should never trigger when we know another search is running
    await store.dispatch(CollectionRequestActions.collectionSearchRequest())

    // trigger search and ask for facets
    const retrieveFacets = true
    store.dispatch(CollectionResultActions.triggerSearch(retrieveFacets))

    const actualCollections = store.getState().search.collectionResult
      .collections
    const expectedCollections = {}
    const actualFacets = store.getState().search.collectionResult.facets
    const expectedFacets = {}
    expect(actualCollections).toEqual(expectedCollections)
    expect(actualFacets).toEqual(expectedFacets)
  })

  it('collectionUpdateQueryText sets queryText', async () => {
    const queryTextBefore = stateBefore.behavior.collectionFilter.queryText
    const newQueryText = 'bermuda triangle'
    // update search query via redux store action
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText(newQueryText)
    )
    const queryTextAfter = store.getState().behavior.collectionFilter.queryText
    expect(queryTextAfter).not.toBe(queryTextBefore)
    expect(queryTextAfter).toBe(newQueryText)
  })

  it('collectionSearchRequest sets collectionSearchRequestInFlight', async () => {
    const collectionInFlightBefore =
      stateBefore.behavior.request.collectionSearchRequestInFlight
    // the `collectionSearchRequest` action is what triggers the `collectionSearchRequestInFlight` to true
    await store.dispatch(CollectionRequestActions.collectionSearchRequest())
    const collectionInFlightAfter = store.getState().behavior.request
      .collectionSearchRequestInFlight
    expect(collectionInFlightBefore).not.toBeTruthy()
    expect(collectionInFlightAfter).toBeTruthy()
  })

  it('collectionSearchSuccess sets result items and resets collectionSearchRequestInFlight to false', async () => {
    const collectionsBefore = stateBefore.search.collectionResult.collections
    // the `collectionSearchRequest` action is what triggers the `collectionSearchRequestInFlight` to true
    await store.dispatch(CollectionRequestActions.collectionSearchRequest())
    // the mock items to "complete" the search with
    const items = new Map()
    items.set('data1', {
      type: 'collection',
      importantInfo1: 'this is important',
      importantInfo2: 'but this is more important',
    })
    items.set('data2', {
      type: 'collection',
      importantInfo3: 'what could possibly be this important?',
      importantInfo4: 'how about this!',
    })

    await store.dispatch(
      CollectionRequestActions.collectionSearchSuccess(items)
    )
    const collectionsAfter = store.getState().search.collectionResult
      .collections
    const collectionInFlightAfter = store.getState().behavior.request
      .collectionSearchRequestInFlight

    const expectedCollectionKeys = Array.from(items.keys())
    const actualCollectionsKeys = Object.keys(collectionsAfter)

    expect(collectionsBefore).toEqual({})
    expect(actualCollectionsKeys).toEqual(expectedCollectionKeys)
    expect(collectionInFlightAfter).not.toBeTruthy()
  })
})

describe('The granule actions', () => {
  let url = '/'
  let urlSearchGranule = '/-search/search/granule'
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

  it('fetches granules with selected collections', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchGranule}`, mockSearchGranuleResponse)

    // update selected collection ids via `collectionToggleSelectedId` action
    const collectionIds = [ 'A', 'B' ]
    await Promise.all(
      collectionIds.map(collectionId => {
        return store.dispatch(
          CollectionFilterActions.collectionToggleSelectedId(collectionId)
        )
      })
    )

    // trigger search
    await store.dispatch(CollectionResultActions.fetchGranules())

    const actualGranules = store.getState().search.collectionResult.granules
    const expectedGranules = {
      '1': {
        id: 1,
        title: 'one',
      },
      '2': {
        id: 2,
        title: 'two',
      },
    }
    expect(actualGranules).toEqual(expectedGranules)
  })

  it('fetches granules with selected collections, queryText, and selectedFacets', async () => {
    // mock search request & response
    fetchMock.post(`path:${urlSearchGranule}`, mockSearchGranuleResponse)

    // update selected collection ids via `collectionToggleSelectedId` action
    const collectionIds = [ 'A', 'B' ]
    await Promise.all(
      collectionIds.map(collectionId => {
        return store.dispatch(
          CollectionFilterActions.collectionToggleSelectedId(collectionId)
        )
      })
    )

    // update search query via redux store action
    const newQueryText = 'bermuda triangle'
    await store.dispatch(
      CollectionFilterActions.collectionUpdateQueryText(newQueryText)
    )

    // updated selected facets via `collectionToggleFacet` action
    const selectedFacets = [
      {
        category: 'science',
        facetName: 'Agriculture',
        selected: true,
      },
    ]
    await Promise.all(
      selectedFacets.map(facet => {
        return store.dispatch(
          collectionToggleFacet(facet.category, facet.facetName, facet.selected)
        )
      })
    )

    // trigger search
    await store.dispatch(CollectionResultActions.fetchGranules())

    const actualGranules = store.getState().search.collectionResult.granules
    const expectedGranules = {
      '1': {
        id: 1,
        title: 'one',
      },
      '2': {
        id: 2,
        title: 'two',
      },
    }
    const actualQueryText = store.getState().search.collectionFilter.queryText
    const actualSelectedFacets = store.getState().search.collectionFilter
      .selectedFacets
    const actualNumScienceFacets = actualSelectedFacets['science'].length
    const expectedNumScienceFacets = selectedFacets.filter(facet => {
      return facet.category === 'science'
    }).length

    expect(actualGranules).toEqual(expectedGranules)
    expect(actualQueryText).toBe(newQueryText)
    expect(actualNumScienceFacets).toBe(expectedNumScienceFacets)
  })
})
