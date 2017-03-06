import { connect } from 'react-redux'
import SearchFieldsComponent from './SearchFieldsComponent'
import { triggerSearch, clearFacets, clearCollections } from '../actions/SearchRequestActions'
import { updateQuery, updateSearch } from '../actions/SearchParamActions'
import { showCollections } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.behavior.search.queryText,
    startDateTime: state.behavior.search.startDateTime,
    endDateTime: state.behavior.search.endDateTime,
    geoJSON: state.behavior.search.geoJSON
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateQuery: (text) => dispatch(updateQuery(text)),
    clearSearch: () => dispatch(updateSearch())
  }
}

const SearchFieldsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFieldsComponent)

export default SearchFieldsContainer
