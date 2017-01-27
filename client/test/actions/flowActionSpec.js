import '../specHelper'
import _ from 'lodash'
import * as actions from '../../src/actions/FlowActions'
import reducer from '../../src/reducers/reducer'
import { expect, assert } from 'chai'
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import sinon from 'sinon'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('The flow actions', function () {

  const initialState = reducer(undefined, {})
  const mockState = {
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
      }
    }
  }

  it('trigger search & update state', function () {
    const behaviorState = {behavior: {search: {selectedIds: []}}}
    //const behaviorState = {behavior: {search: {selectedIds: [123]}}}
    const tempState = _.merge(mockState, behaviorState)

    const store = mockStore(tempState)
    const dispatch = sinon.spy(store, 'dispatch')
    const fn = actions.initialize()

    fn(dispatch, store.getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 2, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('triggers search & fetches granules', function () {
    const behaviorState = {behavior: {search: {selectedIds: [123]}}}
    const tempState = _.merge(mockState, behaviorState)

    const store = mockStore(tempState)
    const dispatch = sinon.spy(store, 'dispatch')
    const fn = actions.initialize()

    fn(dispatch, store.getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 3, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('dispatch a transition to the collections view', function () {
    const store = mockStore(mockState)
    const dispatch = sinon.spy(store, 'dispatch')
    const fn = actions.initialize()

    fn(dispatch, store.getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 3, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('dispatch a transition to the granules view', function () {
    const store = mockStore(mockState)
    const dispatch = sinon.spy(store, 'dispatch')
    const fn = actions.initialize()

    fn(dispatch, store.getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 3, `There were ${dispatchCalls} dispatch calls made`)
  })
})
