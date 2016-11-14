import { connect } from 'react-redux'
import { setFocus } from '../../detail/DetailActions'
import ResultsList from './CollectionGridComponent'

const mapStateToProps = (state) => {
  return {
    loading: state.getIn(['search', 'inFlight']),
    results: state.get('results')
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
)(ResultsList)

export default CollectionGridContainer
