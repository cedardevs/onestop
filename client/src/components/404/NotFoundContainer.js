import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import NotFound from './NotFound'
import {
  asyncNewCollectionSearch,
  showCollections,
} from '../../actions/search/CollectionSearchActions'
import {collectionUpdateQueryText} from '../../actions/search/CollectionFilterActions'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(asyncNewCollectionSearch())
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
