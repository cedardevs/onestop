import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import CollectionSearch from './CollectionSearch'
import {
  collectionUpdateQueryText,
  collectionClearFilters,
} from '../../../actions/routing/CollectionSearchStateActions'
import {asyncNewCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.collectionFilter.queryText,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(collectionClearFilters())
      dispatch(asyncNewCollectionSearch(ownProps.history))
    },
    collectionUpdateQueryText: text =>
      dispatch(collectionUpdateQueryText(text)),
  }
}

const CollectionSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionSearch)
)

export default CollectionSearchContainer
