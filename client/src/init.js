import store from './store'
import watch from 'redux-watch'
import {isDetailPage, isGranuleListPage, isSitemap} from './utils/urlUtils'
import {
  initialize,
  loadCollections,
  loadDetails,
  loadDetailsIsolated,
  loadGranulesList,
} from './actions/InitActions'
import {fetchSitemap} from './actions/fetch/FetchActions'
import history from './history'

const loadFromUrl = (path, newQueryString) => {
  if (isSitemap(path)) {
    store.dispatch(fetchSitemap())
  }
  else if (isDetailPage(path)) {
    store.dispatch(loadDetails(history, path, newQueryString))
  }
  else if (isGranuleListPage(path)) {
    store.dispatch(loadGranulesList(history, path, newQueryString))

    // If `state.search.collectionDetailResult.collection` is null, then we've landed here cold,
    // and we don't have collection details, so we will send that request to populate the collection information
    // based on the collection ID taken from the URL and placed into `state.search.granuleFilter.selectedCollectionIds[0]`
    // Note: we never actually use multiple collection IDs, so we should reliably be able to take the first element
    // until we decide to change that behavior.
    const collection = store.getState().search.collectionDetailResult.collection
    const collectionIdFromUrl = store.getState().search.granuleFilter
      .selectedCollectionIds[0]
    if (!collection && collectionIdFromUrl) {
      store.dispatch(loadDetailsIsolated(collectionIdFromUrl))
    }
  }
  else {
    store.dispatch(loadCollections(history, newQueryString))
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
