import {connect} from 'react-redux'
import {
  insertSelectedGranule,
  removeSelectedGranule,
} from '../../../actions/CartActions'

import GranuleList from './GranuleList'

import {withRouter} from 'react-router'
import {
  submitGranuleSearchForCart,
  submitGranuleSearchWithPage,
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
    collectionTitle: collectionDetail
      ? collectionDetail.attributes.title
      : null,
    results: granules,
    totalHits: totalGranuleCount,
    returnedHits: loadedGranuleCount,
    selectedGranules: state.cart.selectedGranules,
    featuresEnabled: state.config.featuresEnabled,
    granuleFilter: state.search.granuleFilter,
    addFilteredGranulesToCartWarning: state.cart.error,
    loading: state.search.granuleRequest.inFlight,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    fetchResultPage: (offset, max) => {
      dispatch(submitGranuleSearchWithPage(offset, max))
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
      dispatch(insertSelectedGranule(item, itemId))
    },
    deselectGranule: itemId => {
      dispatch(removeSelectedGranule(itemId))
    },
  }
}

const GranuleListContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleList)
)

export default GranuleListContainer
