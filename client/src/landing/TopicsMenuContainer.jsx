import {connect} from 'react-redux'
import TopicsMenu from './TopicsMenu'
import {triggerSearch, clearFacets} from '../actions/SearchRequestActions'
import {updateQuery} from '../actions/SearchParamActions'
import {showCollections} from '../actions/FlowActions'

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

const TopicsMenuContainer = connect(null, mapDispatchToProps)(TopicsMenu)

export default TopicsMenuContainer
