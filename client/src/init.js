import store from './store'
import watch from 'redux-watch'
import {isDetailPage, isGranuleListPage, isSitemap} from './utils/urlUtils'
import {
  getSitemap,
  initialize,
  loadCollections,
  loadDetails,
  loadGranulesList,
} from './actions/InitActions'
import history from './history'

const loadFromUrl = (path, newQueryString) => {
  if (isSitemap(path)) {
    store.dispatch(getSitemap())
  }
  else if (isDetailPage(path)) {
    store.dispatch(loadDetails(path))
  }
  else if (isGranuleListPage(path)) {
    store.dispatch(loadGranulesList(history, path, newQueryString))
  }
  else {
    store.dispatch(loadCollections(newQueryString))
  }
}

store.dispatch(initialize())

const routingWatch = watch(store.getState, 'routing.locationBeforeTransitions')
const routingUpdates = locationBeforeTransitions => {
  loadFromUrl(
    locationBeforeTransitions.location.pathname,
    locationBeforeTransitions.location.search
  )
}
store.subscribe(routingWatch(routingUpdates))
