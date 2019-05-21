import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import CollectionSearch from './CollectionSearch'
import {collectionUpdateQueryText} from '../../../actions/routing/CollectionSearchStateActions'
import {submitCollectionSearchWithQueryText} from '../../../actions/routing/CollectionSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.collectionFilter.queryText,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: text => {
      dispatch(submitCollectionSearchWithQueryText(ownProps.history, text))
    },
    collectionUpdateQueryText: text =>
      dispatch(collectionUpdateQueryText(text)), // TODO is this prop function really needed... sorta maybe?
  }
}

const CollectionSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionSearch)
)

export default CollectionSearchContainer
