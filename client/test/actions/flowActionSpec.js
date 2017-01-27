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

  let dispatch
  beforeEach(() => {
    dispatch = sinon.stub()
  })

  it('trigger search & update state', function () {
    const behaviorState = {behavior: {search: {selectedIds: []}}}
    const tempState = _.merge(mockState, behaviorState)

    const store = mockStore(tempState)
    const fn = actions.initialize()

    const getState = sinon.stub().returns(tempState)
    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 2, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('triggers search & fetches granules', function () {
    const behaviorState = {behavior: {search: {selectedIds: [123]}}}
    const tempState = _.merge(mockState, behaviorState)

    const getState = sinon.stub().returns(tempState)
    const fn = actions.initialize()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 3, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('dispatch a transition to the collections view', function () {
    const getState = sinon.stub().returns(mockState)
    const fn = actions.showCollections()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 1, `There were ${dispatchCalls} dispatch calls made`)
  })

  it('dispatch a transition to the granules view', function () {
    const getState = sinon.stub().returns(mockState)
    const fn = actions.showGranules()

    fn(dispatch, getState)
    const dispatchCalls = dispatch.callCount
    assert( dispatchCalls == 1, `There were ${dispatchCalls} dispatch calls made`)
  })
})
