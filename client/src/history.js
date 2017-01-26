import { syncHistoryWithStore } from 'react-router-redux'
import { hashHistory } from 'react-router'
import store from './store.js'

const history = syncHistoryWithStore(hashHistory, store, {
  selectLocationState: (state) => state.routing
})

export default history
