import store from './store'
import watch from 'redux-watch'
import {
  initialize,
  loadDetails,
  loadCollections,
  loadGranulesList,
} from './actions/FlowActions'
import {isDetailPage, isGranuleListPage} from './utils/urlUtils'

const loadFromUrl = (path, newQueryString) => {
  if (isDetailPage(path)) {
    store.dispatch(loadDetails(path))
  }
  else if (isGranuleListPage(path)) {
    store.dispatch(loadGranulesList(path))
  }
  else {
    store.dispatch(loadCollections(newQueryString))
  }
}

store.dispatch(initialize())

const routingWatch = watch(
  store.getState,
  'behavior.routing.locationBeforeTransitions'
)
const routingUpdates = locationBeforeTransitions => {
  loadFromUrl(
    locationBeforeTransitions.pathname,
    locationBeforeTransitions.search
  )
}
store.subscribe(routingWatch(routingUpdates))
