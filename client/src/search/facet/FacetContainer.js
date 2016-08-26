import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { triggerSearch, updateQuery } from '../SearchActions'

const mapStateToProps = (state) => {
  return {
    categories: state.getIn(['facets', 'categories']).toJS()
  }
}

const mapDispatchToProps = (dispatch) => {
  return {}
}

const FacetContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetList)

export default FacetContainer
