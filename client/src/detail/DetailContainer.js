import {connect} from 'react-redux'
import {fetchGranules, clearGranules} from '../actions/SearchRequestActions'
import {toggleSelection, clearSelections} from '../actions/SearchParamActions'
import {setFocus} from '../actions/FlowActions'
import Detail from './Detail'

const mapStateToProps = (state, reactProps) => {
  const {focusedId} = state.ui.cardDetails
  const focusedItem = state.domain.results.collections[focusedId]
  return {
    id: focusedId,
    item: focusedItem,
    loading: state.ui.loading,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    // TODO back button in browser - does it set focus to null in redux state???
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
