import { connect } from 'react-redux'
import { setFocus } from './DetailActions'
import { clearSearch, triggerSearch, updateQuery } from '../search/SearchActions'
import { clearFacets } from '../search/facet/FacetActions'
import Detail from './DetailComponent'

const mapStateToProps = (reduxState, reactProps) => {
  const focusedId = reduxState.get('details').get('focusedId')
  const focusedItem = reduxState.get('results').get(focusedId)
  return {
    id: focusedId,
    item: focusedItem ? focusedItem.toJS() : null
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    dismiss: () => dispatch(setFocus(null)),
    textSearch: (text) => {
      dispatch(clearFacets())
      dispatch(clearSearch())
      dispatch(updateQuery(text))
      dispatch(triggerSearch())
    }
  }
}

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Detail)

export default DetailContainer
