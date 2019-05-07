import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import NotFound from './NotFound'
import {
  triggerSearch,
  showCollections,
} from '../../actions/search/CollectionSearchActions'
import {
  collectionClearFacets,
  collectionUpdateQueryText,
} from '../../actions/search/CollectionFilterActions'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(collectionClearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    collectionUpdateQueryText: text => {
      dispatch(collectionUpdateQueryText(text))
    },
  }
}

const NotFoundContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(NotFound)
)

export default NotFoundContainer
