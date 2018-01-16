import {connect} from 'react-redux'
import SearchFields from './SearchFields'
import {
  triggerSearch,
  clearCollections,
} from '../actions/SearchRequestActions'
import {
  removeAllFilters, updateQuery,
  updateSearch
} from '../actions/SearchParamActions'
import {showCollections} from '../actions/FlowActions'

const mapStateToProps = state => {
  return {
    queryString: state.behavior.search.queryText,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    submit: () => {
      dispatch(removeAllFilters())
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateQuery: text => dispatch(updateQuery(text)),
    clearSearch: () => dispatch(updateSearch()),
  }
}

const SearchFieldsContainer = connect(mapStateToProps, mapDispatchToProps)(
  SearchFields
)

export default SearchFieldsContainer
