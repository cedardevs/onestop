import { connect } from 'react-redux'
import { setFocus } from '../../detail/DetailActions'
import CollectionGrid from './CollectionGridComponent'

const mapStateToProps = (state) => {
  return {
    results: state.collections.results,
    totalHits: state.collections.totalHits,
    returnedHits: state.collections.results && Object.keys(state.collections.results).length || 0, // TODO - use the total hits from the search response
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(setFocus(id))
    }
  }
}

const CollectionGridContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(CollectionGrid)

export default CollectionGridContainer
