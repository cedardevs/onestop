import {connect} from 'react-redux'
import {
  incrementGranulesOffset,
  fetchGranules,
} from '../../actions/SearchRequestActions'
import {
  insertSelectedGranule,
  insertMultipleSelectedGranules,
  removeSelectedGranule,
  removeMultipleSelectedGranules,
} from '../../actions/CartActions'
import {insertGranule, removeGranuleFromLocalStorage, getSelectedGranulesFromStorage} from '../../utils/localStorageUtil'

import GranuleList from './GranuleList'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {granules, totalGranules} = state.domain.results
  const focusedItem = state.domain.results.collectionDetail
  return {
    collectionTitle: focusedItem
      ? focusedItem.collection.attributes.title
      : null,
    results: granules,
    totalHits: totalGranules,
    returnedHits: (granules && Object.keys(granules).length) || 0,
    loading: state.ui.loading ? 1 : 0,
    selectedGranules: getSelectedGranulesFromStorage(state),
    featuresEnabled: state.domain.config.featuresEnabled,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchMoreResults: () => {
      dispatch(incrementGranulesOffset())
      dispatch(fetchGranules(false))
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
