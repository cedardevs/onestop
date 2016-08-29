import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { triggerSearch, updateQuery } from '../SearchActions'
import { updateFacetsSelected } from './FacetActions'

const mapStateToProps = (state) => {
  return {
    categories: state.getIn(['facets', 'categories']).toJS()
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    updateFacets: (facet) => dispatch(updateFacetsSelected(facet)),
    submit: () => dispatch(triggerSearch())
  }
}

const FacetContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetList)

export default FacetContainer
