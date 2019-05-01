import {connect} from 'react-redux'
import SearchFields from './SearchFields'
import {triggerSearch, clearCollections} from '../actions/SearchRequestActions'
import {
  removeAllFilters,
  updateQuery,
  updateSearch,
} from '../actions/SearchParamActions'
import {showCollections} from '../actions/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    queryString: state.behavior.search.queryText,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(removeAllFilters())
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    updateQuery: text => dispatch(updateQuery(text)),
    clearSearch: () => dispatch(updateSearch()),
  }
}

const SearchFieldsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(SearchFields)
)

export default SearchFieldsContainer
