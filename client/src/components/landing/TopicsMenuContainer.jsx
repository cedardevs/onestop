import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TopicsMenu from './TopicsMenu'
import {} from '../../actions/search/CollectionResultActions'
import {collectionUpdateQueryText} from '../../actions/search/CollectionFilterActions'
import {asyncNewCollectionSearch} from '../../actions/search/CollectionSearchActions'

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
