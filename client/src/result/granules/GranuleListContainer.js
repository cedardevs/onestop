import {connect} from 'react-redux'
import {showCollections, toggleGranuleFocus} from '../../actions/FlowActions'
import {
  incrementGranulesOffset,
  fetchGranules,
} from '../../actions/SearchRequestActions'
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
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchMoreResults: () => {
      dispatch(incrementGranulesOffset())
      dispatch(fetchGranules(false))
    },
  }
}

const GranuleListContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleList)
)

export default GranuleListContainer
