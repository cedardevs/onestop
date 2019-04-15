import {connect} from 'react-redux'
import CollectionSearch from './CollectionSearch'
import {
  triggerSearch,
  clearCollections,
} from '../../../actions/SearchRequestActions'
import {
  removeAllFilters,
  updateQuery,
  updateSearch,
} from '../../../actions/SearchParamActions'
import {showCollections} from '../../../actions/FlowActions'

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

const CollectionSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionSearch)
)

export default CollectionSearchContainer