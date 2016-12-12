import { connect } from 'react-redux'
import { setFocus } from '../../detail/DetailActions'
import CollectionGrid from './CollectionGridComponent'

const mapStateToProps = (state) => {
  return {
    results: state.get('collections').get('results'),
    totalHits: state.get('collections').get('totalHits'),
    returnedHits: state.get('collections').get('results').count()
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
