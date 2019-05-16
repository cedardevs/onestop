import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TopicsMenu from './TopicsMenu'
import {} from '../../actions/search/CollectionResultActions'
import {
  collectionClearFacets,
  collectionUpdateQueryText,
} from '../../actions/search/CollectionFilterActions'
import {
  triggerSearch,
  showCollections,
} from '../../actions/search/SearchActions'

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

const TopicsMenuContainer = withRouter(
  connect(null, mapDispatchToProps)(TopicsMenu)
)

export default TopicsMenuContainer
