import { connect } from 'react-redux'
import SearchFieldsComponent from './SearchFieldsComponent'
import { triggerSearch, updateQuery, clearSearch } from './SearchActions'
import { clearFacets } from './facet/FacetActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.appState.search.queryText,
    startDateTime: state.appState.search.startDateTime,
    endDateTime: state.appState.search.endDateTime,
    geoJSON: state.appState.search.geoJSON
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
