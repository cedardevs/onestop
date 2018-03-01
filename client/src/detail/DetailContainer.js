import {connect} from 'react-redux'
import {fetchGranules, clearGranules} from '../actions/SearchRequestActions'
import {toggleSelection, clearSelections} from '../actions/SearchParamActions'
import Detail from './Detail'

const mapStateToProps = (state, reactProps) => {
  const focusedItem = state.domain.results.collectionDetail
  return {
    item: focusedItem ? focusedItem.collection.attributes : null,
    totalGranuleCount: focusedItem ? focusedItem.totalGranuleCount : null,
    loading: state.ui.loading,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    showGranules: id => {
      dispatch(clearSelections())
      dispatch(toggleSelection(id))
      dispatch(clearGranules())
      dispatch(fetchGranules())
    },
  }
}

const DetailContainer = connect(mapStateToProps, mapDispatchToProps)(Detail)

export default DetailContainer
