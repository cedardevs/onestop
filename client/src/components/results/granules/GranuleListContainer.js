import {connect} from 'react-redux'
import {
  insertSelectedGranule,
  removeSelectedGranule,
} from '../../../actions/CartActions'
import {
  insertSelectedGranuleIntoLocalStorage,
  removeSelectedGranuleFromLocalStorage,
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
  const collectionDetail = state.search.collectionDetailResult.collection

  return {
    collectionId: collectionDetail ? collectionDetail.id : null,
    collectionTitle: collectionDetail ? collectionDetail.attributes.title : null,
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
      insertSelectedGranuleIntoLocalStorage(itemId, item)
      dispatch(insertSelectedGranule(item, itemId))
    },
    deselectGranule: itemId => {
      removeSelectedGranuleFromLocalStorage(itemId)
      dispatch(removeSelectedGranule(itemId))
    },
  }
}

const GranuleListContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleList)
)

export default GranuleListContainer
