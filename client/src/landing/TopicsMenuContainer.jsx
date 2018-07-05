import {connect} from 'react-redux'
import TopicsMenu from './TopicsMenu'
import {triggerSearch, clearFacets} from '../actions/SearchRequestActions'
import {updateQuery} from '../actions/SearchParamActions'
import {showCollections} from '../actions/FlowActions'

import {withRouter} from 'react-router'

const mapDispatchToProps = dispatch => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateQuery: text => dispatch(updateQuery(text)),
  }
}

const TopicsMenuContainer = withRouter(
  connect(null, mapDispatchToProps)(TopicsMenu)
)

export default TopicsMenuContainer
