import { connect } from 'react-redux'
import SearchFieldsComponent from './SearchFieldsComponent'
import { triggerSearch, updateQuery, clearSearch } from './SearchActions'
import { clearFacets } from './facet/FacetActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.getIn(['search', 'text']),
    startDateTime: state.getIn(['search', 'startDateTime']),
    endDateTime: state.getIn(['search', 'endDateTime']),
    geoJSON: state.getIn(['search', 'geoJSON'])
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
    },
    updateQuery: (text) => dispatch(updateQuery(text)),
    clearSearch: () => dispatch(clearSearch())
  }
}

const SearchFieldsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFieldsComponent)

export default SearchFieldsContainer
