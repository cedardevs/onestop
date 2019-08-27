import {connect} from 'react-redux'
import {
  insertSelectedGranule,
  removeSelectedGranule,
} from '../../../actions/CartActions'
import {
  insertGranule,
  removeGranuleFromLocalStorage,
  getSelectedGranulesFromStorage,
} from '../../../utils/localStorageUtil'

import GranuleList from './GranuleList'

import {withRouter} from 'react-router'
import {
  submitGranuleSearchForCart,
  submitGranuleSearchNextPage,
} from '../../../actions/routing/GranuleSearchRouteActions'
import {CART_CAPACITY, MAX_CART_ADDITION} from '../../../utils/cartUtils'

const mapStateToProps = state => {
  const {
    granules,
    totalGranuleCount,
    loadedGranuleCount,
  } = state.search.granuleResult
  const focusedItem = state.search.collectionDetailResult.collection

  return {
    collectionTitle: focusedItem ? focusedItem.attributes.title : null,
    results: granules,
    totalHits: totalGranuleCount,
    returnedHits: loadedGranuleCount,
    selectedGranules: getSelectedGranulesFromStorage(state),
    featuresEnabled: state.config.featuresEnabled,
    granuleFilter: state.search.granuleFilter,
    addFilteredGranulesToCartWarning: state.cart.error,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    fetchMoreResults: () => {
      dispatch(submitGranuleSearchNextPage())
    },
    addFilteredGranulesToCart: granuleFilter => {
      dispatch(
        submitGranuleSearchForCart(
          ownProps.history,
          granuleFilter,
          MAX_CART_ADDITION,
          CART_CAPACITY
        )
      )
    },
    selectGranule: (item, itemId) => {
      insertGranule(itemId, item)
      dispatch(insertSelectedGranule(item, itemId))
    },
    deselectGranule: itemId => {
      removeGranuleFromLocalStorage(itemId)
      dispatch(removeSelectedGranule(itemId))
    },
  }
}

const GranuleListContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleList)
)

export default GranuleListContainer
