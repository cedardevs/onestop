import '../specHelper'
import fetchMock from 'fetch-mock'
import React from 'react'
import {mount} from 'enzyme'
import App from '../../src/App'
import store from '../../src/store' // create Redux store with appropriate middleware
import history from '../../src/history'

import * as FlowActions from '../../src/actions/FlowActions'
import {RESET_STORE} from '../../src/reducers/reducer'
import {API_PATH} from '../../src/utils/urlUtils'
import {
  mockCollectionCountResponse,
  mockGranuleCountResponse,
} from '../mockCount'
import {mockConfigResponse} from '../mockConfig'
import {mockInfoResponse} from '../mockInfo'

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

  before(() => {
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
    fetchMock.get(`path:${API_PATH}/uiConfig`, mockConfigResponse)

    // mock fetch info
    fetchMock.get(`path:${API_PATH}/actuator/info`, mockInfoResponse)

    // mock fetch collection & granule counts
    fetchMock.get(`path:${API_PATH}/collection`, mockCollectionCountResponse)
    fetchMock.get(`path:${API_PATH}/granule`, mockGranuleCountResponse)

    // debugStore("BEFORE")

    // trigger initialize
    // store.dispatch(FlowActions.initialize()).then(() => {
    //   debugStore("THEN1")
    // })

    // debugStore("AFTER")
    ///

    // TODO: is there a dangling event handler here?
    // why do we have to use `mocha --exit` or call `done()` in this test for mocha to exit properly?
    // done()

    // const actualCollections = store.getState().domain.results.collections
    // const expectedCollections = {
    //   '123ABC': {
    //     type: 'collection',
    //     field0: 'field0',
    //     field1: 'field1',
    //   },
    //   '789XYZ': {
    //     type: 'collection',
    //     field0: 'field00',
    //     field1: 'field01',
    //   },
    // }
    // const actualFacets = store.getState().domain.results.facets
    // const expectedFacets = {
    //   science: [
    //     {
    //       term: 'land',
    //       count: 2,
    //     },
    //   ],
    // }
    // actualCollections.should.deep.equal(expectedCollections)
    // actualFacets.should.deep.equal(expectedFacets)
  }).timeout(10000)

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
  //
  //   console.log("before: ", browserHistoryPushStub)
  //
  //
  //   const fn = actions.showCollections()
  //
  //   console.log("after: ", browserHistoryPushStub)
  //
  //   fn(dispatch, getState)
  //   const dispatchCalls = dispatch.callCount
  //   assert(dispatchCalls === 1, `There were ${dispatchCalls} dispatch calls made`)
  // })
  //
  // it('dispatch a clearSelections action and transition to the collections view when search params are present', function () {
  //   const stateWithSearchParams = _.merge(mockDefaultState, {behavior: {search: {queryText: 'oceans'}}})
  //   const getState = sinon.stub().returns(stateWithSearchParams)
  //   const fn = actions.showCollections()
  //
  //   fn(dispatch, getState)
  //   const dispatchCalls = dispatch.callCount
  //   assert(dispatchCalls === 2, `There were ${dispatchCalls} dispatch calls made`)
  // })
})
