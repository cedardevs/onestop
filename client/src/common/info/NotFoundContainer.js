import {connect} from 'react-redux'
import NotFound from './NotFound'

import {withRouter} from 'react-router'
import {clearFacets, triggerSearch} from '../../actions/SearchRequestActions'
import {showCollections} from '../../actions/FlowActions'
import {updateQuery} from '../../actions/SearchParamActions'

const mapStateToProps = state => {
  return {}
}

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

const NotFoundContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(NotFound)
)

export default NotFoundContainer
