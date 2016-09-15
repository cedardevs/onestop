import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { triggerSearch, updateQuery } from '../SearchActions'
import { updateFacetsSelected } from './FacetActions'

const mapStateToProps = (state) => {
  return {
    categories: state.getIn(['facets', 'categories']) ? state.getIn(['facets', 'categories']).toJS() : {}
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    updateFacets: facetsSelected => dispatch(updateFacetsSelected(facetsSelected)),
    submit: () => dispatch(triggerSearch(null, null, true))
  }
}

const FacetContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetList)

export default FacetContainer
