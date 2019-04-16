import fetchMock from 'fetch-mock'
import React from 'react'
import {mount} from 'enzyme'
import App from '../../src/App'
import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'

import {RESET_STORE} from '../../src/reducer'
import {apiPath} from '../../src/utils/urlUtils'
import {
  mockCollectionCountResponse,
  mockGranuleCountResponse,
} from '../mocks/mockCount'
import {mockConfigResponse} from '../mocks/mockConfig'
import {mockInfoResponse} from '../mocks/mockInfo'

const debugStore = (label, path) => {
  const state = store.getState()
  const stateSelector = _.get(state, path, state)
  console.log('%s:\n\n%s', label, JSON.stringify(stateSelector, null, 4))
}

describe('The flow actions', () => {
  let url = '/'
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

  it('initialize triggers config, version info, total counts, and data loading', async () => {
    // mock fetch config
    fetchMock.get(`path:${apiPath()}/uiConfig`, mockConfigResponse)

    // mock fetch info
    fetchMock.get(`path:${apiPath()}/actuator/info`, mockInfoResponse)

    // mock fetch collection & granule counts
    fetchMock.get(`path:${apiPath()}/collection`, mockCollectionCountResponse)
    fetchMock.get(`path:${apiPath()}/granule`, mockGranuleCountResponse)

    // debugStore("BEFORE")

    // trigger initialize
    // store.dispatch(FlowActions.initialize()).then(() => {
    //   debugStore("THEN1")
    // })

    // debugStore("AFTER")
    ///

    // TODO: is there a dangling event handler here?
    // why do we have to use `mocha --exit` in this test for mocha to exit properly?
    // we'll keep this commented out until we consider changes to initialize()
  })

  // TODO: rewrite these tests with new testing paradigm
  // describe('loadData', function () {
  //   it('loads only collections if no ids are selected', function () {
  //     const getState = sinon.stub().returns(mockDefaultState)
  //     const fn = actions.loadCollections()
  //
  //     fn(dispatch, getState)
  //     const dispatchCalls = dispatch.callCount
  //     assert(dispatchCalls === 1, `There were ${dispatchCalls} dispatch calls made`)
  //   })
  //
  //   it('loads collections and granules if ids are selected', function () {
  //     const stateWithIds = _.merge(mockDefaultState, {behavior: {search: {selectedIds: [123]}}})
  //     const getState = sinon.stub().returns(stateWithIds)
  //     const fn = actions.loadCollections()
  //
  //     fn(dispatch, getState)
  //     const dispatchCalls = dispatch.callCount
  //     assert(dispatchCalls === 2, `There were ${dispatchCalls} dispatch calls made`)
  //   })
  // })

  // it('do not dispatch a transition to the collections view, just a clearSelections action, when no search params are present', function () {
  //   const getState = sinon.stub().returns(mockDefaultState)
  //   const fn = actions.showCollections()
  //
  //   fn(dispatch, getState)
  //   expect(dispatch.callCount).toBe(1)
  // })
  //
  // it('dispatch a clearSelections action and transition to the collections view when search params are present', function () {
  //   const stateWithSearchParams = _.merge(mockDefaultState, {behavior: {search: {queryText: 'oceans'}}})
  //   const getState = sinon.stub().returns(stateWithSearchParams)
  //   const fn = actions.showCollections()
  //
  //   fn(dispatch, getState)
  //   expect(dispatch.callCount).toBe(2)
  // })
})
