import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import CollectionSearch from './CollectionSearch'
import {collectionClearResults} from '../../../actions/search/CollectionResultActions'
import {
  collectionRemoveFilters,
  collectionUpdateQueryText,
  collectionUpdateFilters,
} from '../../../actions/search/CollectionFilterActions'
import {
  triggerSearch,
  showCollections,
} from '../../../actions/search/CollectionSearchActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.collectionFilter.queryText,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(collectionRemoveFilters())
      dispatch(collectionClearResults())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    collectionUpdateQueryText: text =>
      dispatch(collectionUpdateQueryText(text)),
    clearSearch: () => dispatch(collectionUpdateFilters()),
  }
}

const CollectionSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionSearch)
)

export default CollectionSearchContainer
