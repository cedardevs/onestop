import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TopicsMenu from './TopicsMenu'
import {collectionUpdateQueryText} from '../../actions/routing/CollectionSearchStateActions'
import {asyncNewCollectionSearch} from '../../actions/routing/CollectionSearchRouteActions'

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

const TopicsMenuContainer = withRouter(
  connect(null, mapDispatchToProps)(TopicsMenu)
)

export default TopicsMenuContainer
