import {connect} from 'react-redux'
import TopicsMenu from './TopicsMenu'
import {
  triggerSearch,
  clearFacets,
} from '../../actions/search/collections/SearchRequestActions'
import {updateQuery} from '../../actions/search/collections/SearchParamActions'
import {showCollections} from '../../actions/search/collections/FlowActions'

import {withRouter} from 'react-router'

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    updateQuery: text => {
      dispatch(updateQuery(text))
    },
  }
}

const TopicsMenuContainer = withRouter(
  connect(null, mapDispatchToProps)(TopicsMenu)
)

export default TopicsMenuContainer
