import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import NotFound from './NotFound'
import {asyncNewCollectionSearch} from '../../actions/routing/CollectionSearchRouteActions'
import {collectionUpdateQueryText} from '../../actions/routing/CollectionSearchStateActions'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(asyncNewCollectionSearch(ownProps.history))
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
