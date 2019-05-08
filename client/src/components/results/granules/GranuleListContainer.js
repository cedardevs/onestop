import {connect} from 'react-redux'
import {granuleIncrementResultsOffset} from '../../../actions/search/GranuleRequestActions'
import {
  insertSelectedGranule,
  insertMultipleSelectedGranules,
  removeSelectedGranule,
  removeMultipleSelectedGranules,
} from '../../../actions/CartActions'
import {
  insertGranule,
  removeGranuleFromLocalStorage,
  getSelectedGranulesFromStorage,
} from '../../../utils/localStorageUtil'

import GranuleList from './GranuleList'

import {withRouter} from 'react-router'
import {triggerGranuleSearch} from '../../../actions/search/GranuleSearchActions'

const mapStateToProps = state => {
  const {granules, totalGranules, loadedGranules} = state.search.granuleResult
  const focusedItem = state.search.collectionResult.collectionDetail
  return {
    collectionTitle: focusedItem
      ? focusedItem.collection.attributes.title
      : null,
    results: granules,
    totalHits: totalGranules,
    returnedHits: loadedGranules,
    loading: state.search.loading ? 1 : 0, // TODO gets passed to ListView
    selectedGranules: getSelectedGranulesFromStorage(state),
    featuresEnabled: state.config.featuresEnabled,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchMoreResults: () => {
      dispatch(granuleIncrementResultsOffset())
      dispatch(triggerGranuleSearch(false, false)) // TODO ???
    },
    selectGranule: (item, itemId) => {
      insertGranule(itemId, item)
      dispatch(insertSelectedGranule(item, itemId))
    },
    selectVisibleGranules: (items, itemIds) => {
      dispatch(insertMultipleSelectedGranules(items, itemIds))
    },
    deselectGranule: itemId => {
      removeGranuleFromLocalStorage(itemId)
      dispatch(removeSelectedGranule(itemId))
    },
    deselectVisibleGranules: itemIds => {
      dispatch(removeMultipleSelectedGranules(itemIds))
    },
  }
}

const GranuleListContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleList)
)

export default GranuleListContainer
