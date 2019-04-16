import store from './store'
import watch from 'redux-watch'
import {
  initialize,
  loadDetails,
  loadCollections,
  loadGranulesList,
} from './actions/search/collections/FlowActions'
import {getSitemap} from './actions/search/collections/SearchRequestActions'
import {isDetailPage, isGranuleListPage, isSitemap} from './utils/urlUtils'

const loadFromUrl = (path, newQueryString) => {
  if (isSitemap(path)) {
    store.dispatch(getSitemap())
  }
  else if (isDetailPage(path)) {
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
    locationBeforeTransitions.location.pathname,
    locationBeforeTransitions.location.search
  )
}
store.subscribe(routingWatch(routingUpdates))
