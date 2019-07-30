import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import CollectionSearch from './CollectionSearch'
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
  }
}

const CollectionSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionSearch)
)

export default CollectionSearchContainer
