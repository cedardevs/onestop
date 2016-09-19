import { connect } from 'react-redux'
import Immutable from 'immutable'
import FacetList from './FacetListComponent'
import { triggerSearch, updateQuery } from '../SearchActions'
import { modifySelectedFacets } from './FacetActions'

const mapStateToProps = (state) => {
  return {
    facetMap: state.getIn(['facets', 'allFacets']) ?
      state.getIn(['facets', 'allFacets']).toJS() : {},
    selectedFacets: state.getIn(['facets', 'selectedFacets']) ?
      state.getIn(['facets', 'selectedFacets']) : Immutable.Map()
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    modifySelectedFacets: selectedFacets => dispatch(modifySelectedFacets(selectedFacets)),
    submit: (processFacets) => dispatch(triggerSearch(null, processFacets))
  }
}

const FacetContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetList)

export default FacetContainer
