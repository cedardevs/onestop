import { syncHistoryWithStore } from 'react-router-redux'
import { hashHistory } from 'react-router'
import store from './store.js'

// Create enhanced history object for router
const createSelectLocationState = () => {
  let prevRoutingState, prevRoutingStateJS
  return (state) => {
    const routingState = state.routing
    if (typeof prevRoutingState === 'undefined' || prevRoutingState !== routingState) {
      prevRoutingStateJS = routingState
    }
    return prevRoutingStateJS
  }
}

const history = syncHistoryWithStore(hashHistory, store, {
  selectLocationState: createSelectLocationState()
})

export default history
