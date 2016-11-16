import { connect } from 'react-redux'
import { setFocus } from '../../detail/DetailActions'
import CollectionGrid from './CollectionGridComponent'

const mapStateToProps = (state) => {
  return {
    results: state.get('collections').get('results'),
    count: state.get('collections').get('results').count() // TODO - use the total hits from the search response
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
