import { connect } from 'react-redux'
import { setFocus } from '../../detail/DetailActions'
import CollectionGrid from './CollectionGridComponent'

const mapStateToProps = (state) => {
  const { collections, totalCollections } = state.domain.results
  return {
    results: collections,
    totalHits: totalCollections,
    returnedHits: collections && Object.keys(collections).length || 0
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
