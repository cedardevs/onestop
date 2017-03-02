import '../specHelper'
import _ from 'lodash'
import * as actions from '../../src/actions/FlowActions'
import { expect, assert } from 'chai'
import sinon from 'sinon'

describe('The flow actions', function () {

  const mockDefaultState = {
     domain: {
        config: {
           apiHost: ''
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
    }
  }

  let dispatch
  beforeEach(() => {
    dispatch = sinon.stub()
  })

  it('initialize triggers config and data loading', function () {
    const getState = sinon.stub().returns(mockDefaultState)
    const fn = actions.initialize()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert(dispatchCalls == 2, `There were ${dispatchCalls} dispatch calls made`)
  })

  describe('loadData', function () {
    it('loads only collections if no ids are selected', function () {
      const getState = sinon.stub().returns(mockDefaultState)
      const fn = actions.loadData()

      fn(dispatch, getState)
      const dispatchCalls = dispatch.callCount
      assert(dispatchCalls == 1, `There were ${dispatchCalls} dispatch calls made`)
    })

    it('loads collections and granules if ids are selected', function () {
      const stateWithIds = _.merge(mockDefaultState, {behavior: {search: {selectedIds: [123]}}})
      const getState = sinon.stub().returns(stateWithIds)
      const fn = actions.loadData()

      fn(dispatch, getState)
      const dispatchCalls = dispatch.callCount
      assert(dispatchCalls == 2, `There were ${dispatchCalls} dispatch calls made`)
    })
  })

  it('do not dispatch a transition to the collections view when no search params are present', function () {
    const getState = sinon.stub().returns(mockDefaultState)
    const fn = actions.showCollections()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert(dispatchCalls == 0, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('dispatch a transition to the collections view when search params are present', function () {
    const stateWithSearchParams = _.merge(mockDefaultState, {behavior: {search: {queryText: 'oceans'}}})
    const getState = sinon.stub().returns(stateWithSearchParams)
    const fn = actions.showCollections()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert(dispatchCalls == 1, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('dispatch a transition to the granules view', function () {
    const getState = sinon.stub().returns(mockDefaultState)
    const fn = actions.showGranules()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert(dispatchCalls == 1, `There were ${dispatchCalls} dispatch calls made`)
  })
})
