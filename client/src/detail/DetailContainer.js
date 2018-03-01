import {connect} from 'react-redux'
import {showGranulesList} from '../actions/FlowActions'
import {fetchGranules, clearGranules} from '../actions/SearchRequestActions'
import {toggleSelection, clearSelections} from '../actions/SearchParamActions'
import Detail from './Detail'

const mapStateToProps = (state, reactProps) => {
  const focusedItem = state.domain.results.collectionDetail
  return {
    id: focusedItem ? focusedItem.collection.id : null,
    item: focusedItem ? focusedItem.collection.attributes : null,
    totalGranuleCount: focusedItem ? focusedItem.totalGranuleCount : null,
    loading: state.ui.loading,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    showGranules: id => {
      dispatch(showGranulesList(id))
    },
  }
}

const DetailContainer = connect(mapStateToProps, mapDispatchToProps)(Detail)

export default DetailContainer
