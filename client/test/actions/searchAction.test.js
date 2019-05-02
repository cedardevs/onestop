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
} from '../mockSearchCollection'

import * as SearchRequestActions from '../../src/actions/SearchRequestActions'
import * as SearchParamActions from '../../src/actions/SearchParamActions'
import {RESET_STORE} from '../../src/reducers/reducer'
import {mockSearchGranuleResponse} from '../mockSearchGranule'
import {toggleFacet} from '../../src/actions/SearchParamActions'

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
    await store.dispatch(SearchParamActions.updateQuery('alaska'))

    // trigger search and ask for facets
    const retrieveFacets = true
    await store.dispatch(SearchRequestActions.triggerSearch(retrieveFacets))

    const actualCollections = store.getState().domain.results.collections
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
    const actualFacets = store.getState().domain.results.facets
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

    // ...omit setting query via `updateQuery` action

    // trigger search and ask for facets
    const retrieveFacets = true
    await store.dispatch(SearchRequestActions.triggerSearch(retrieveFacets))

    const actualCollections = store.getState().domain.results.collections
    const expectedCollections = {}
    const actualFacets = store.getState().domain.results.facets
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
    await store.dispatch(SearchParamActions.updateQuery('alaska'))

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
    await store.dispatch(SearchParamActions.updateQuery('alaska'))

    // the `startSearch` action is what triggers the `collectionInFlight` to true
    // we want to artificially set this after we set a valid query so that we can ensure no results come back
    // in other words, the fetch mocked above should never trigger when we know another search is running
    await store.dispatch(SearchRequestActions.startSearch())

    // trigger search and ask for facets
    const retrieveFacets = true
    store.dispatch(SearchRequestActions.triggerSearch(retrieveFacets))

    const actualCollections = store.getState().domain.results.collections
    const expectedCollections = {}
    const actualFacets = store.getState().domain.results.facets
    const expectedFacets = {}
    expect(actualCollections).toEqual(expectedCollections)
    expect(actualFacets).toEqual(expectedFacets)
  })

  it('updateQuery sets queryText', async () => {
    const queryTextBefore = stateBefore.behavior.search.queryText
    const newQueryText = 'bermuda triangle'
    // update search query via redux store action
    await store.dispatch(SearchParamActions.updateQuery(newQueryText))
    const queryTextAfter = store.getState().behavior.search.queryText
    expect(queryTextAfter).not.toBe(queryTextBefore)
    expect(queryTextAfter).toBe(newQueryText)
  })

  it('startSearch sets collectionInFlight', async () => {
    const collectionInFlightBefore =
      stateBefore.behavior.request.collectionInFlight
    // the `startSearch` action is what triggers the `collectionInFlight` to true
    await store.dispatch(SearchRequestActions.startSearch())
    const collectionInFlightAfter = store.getState().behavior.request
      .collectionInFlight
    expect(collectionInFlightBefore).not.toBeTruthy()
    expect(collectionInFlightAfter).toBeTruthy()
  })

  it('completeSearch sets result items and resets collectionInFlight to false', async () => {
    const collectionsBefore = stateBefore.domain.results.collections
    // the `startSearch` action is what triggers the `collectionInFlight` to true
    await store.dispatch(SearchRequestActions.startSearch())
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

    await store.dispatch(SearchRequestActions.completeSearch(items))
    const collectionsAfter = store.getState().domain.results.collections
    const collectionInFlightAfter = store.getState().behavior.request
      .collectionInFlight

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

    // update selected collection ids via `toggleSelection` action
    const collectionIds = [ 'A', 'B' ]
    await Promise.all(
      collectionIds.map(collectionId => {
        return store.dispatch(SearchParamActions.toggleSelection(collectionId))
      })
    )

    // trigger search
    await store.dispatch(SearchRequestActions.fetchGranules())

    const actualGranules = store.getState().domain.results.granules
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

    // update selected collection ids via `toggleSelection` action
    const collectionIds = [ 'A', 'B' ]
    await Promise.all(
      collectionIds.map(collectionId => {
        return store.dispatch(SearchParamActions.toggleSelection(collectionId))
      })
    )

    // update search query via redux store action
    const newQueryText = 'bermuda triangle'
    await store.dispatch(SearchParamActions.updateQuery(newQueryText))

    // updated selected facets via `toggleFacet` action
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
          toggleFacet(facet.category, facet.facetName, facet.selected)
        )
      })
    )

    // trigger search
    await store.dispatch(SearchRequestActions.fetchGranules())

    const actualGranules = store.getState().domain.results.granules
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
    const actualQueryText = store.getState().behavior.search.queryText
    const actualSelectedFacets = store.getState().behavior.search.selectedFacets
    const actualNumScienceFacets = actualSelectedFacets['science'].length
    const expectedNumScienceFacets = selectedFacets.filter(facet => {
      return facet.category === 'science'
    }).length

    expect(actualGranules).toEqual(expectedGranules)
    expect(actualQueryText).toBe(newQueryText)
    expect(actualNumScienceFacets).toBe(expectedNumScienceFacets)
  })
})
