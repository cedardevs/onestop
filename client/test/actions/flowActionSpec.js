import '../specHelper'
import _ from 'lodash'
import * as actions from '../../src/actions/FlowActions'
import { expect, assert } from 'chai'
import sinon from 'sinon'
// import * as router from 'react-router';

describe('The flow actions', function () {

  const mockDefaultState = {
    domain: {
      api: {
        host: '',
        path: '',
      },
      results: {
        granules: {}
      }
    },
    behavior: {
      request: {
        collectionInFlight: false,
        granuleInFlight: false
      },
      search: {
        selectedIds: []
      }
    },
  }

  let dispatch
  // let browserHistoryPushStub

  beforeEach(() => {
    // router.browserHistory = { push: ()=>{} };
    // browserHistoryPushStub = sinon.stub(router.browserHistory, 'push', () => { });

    dispatch = sinon.stub()
  })

  // it('initialize triggers config, version info, total counts, and data loading', function () {
  //   const getState = sinon.stub().returns(mockDefaultState)
  //   const fn = actions.initialize()
  //
  //   fn(dispatch, getState)
  //   const dispatchCalls = dispatch.callCount
  //   assert(dispatchCalls === 3, `There were ${dispatchCalls} dispatch calls made`)
  // })

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
