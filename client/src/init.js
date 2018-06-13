import store from './store'
import watch from 'redux-watch'
import {initialize, loadData} from './actions/FlowActions'
import {decodeQueryString} from './utils/queryUtils'
import {
  triggerSearch,
  fetchGranules,
  clearCollections,
  clearGranules,
  getCollection,
} from './actions/SearchRequestActions'
import {
  updateSearch,
  clearSelections,
  toggleSelection,
} from './actions/SearchParamActions'
import {
  isDetailPage,
  isGranuleListPage,
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from './utils/urlUtils'

const loadFromUrl = (path, newQueryString) => {
  // Note, collection queries are automatically updated by the URL because the query is parsed into search, which triggers loadData via a watch
  if (
    isDetailPage(path) &&
    !store.getState().behavior.request.getCollectionInFlight
  ) {
    const detailId = getCollectionIdFromDetailPath(path)
    store.dispatch(getCollection(detailId))
  }
  else if (isGranuleListPage(path)) {
    const detailId = getCollectionIdFromGranuleListPath(path)
    store.dispatch(clearSelections())
    store.dispatch(toggleSelection(detailId))
    store.dispatch(clearGranules())
    store.dispatch(fetchGranules())
  }
  else {
    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(store.getState(), 'behavior.search')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      store.dispatch(clearCollections())
      store.dispatch(clearGranules())
      store.dispatch(clearSelections())
      store.dispatch(updateSearch(searchFromQuery))
      store.dispatch(loadData())
    }
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
