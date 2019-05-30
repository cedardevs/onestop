import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TopicsMenu from './TopicsMenu'
import {submitCollectionSearchWithQueryText} from '../../actions/routing/CollectionSearchRouteActions'

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: text => {
      dispatch(submitCollectionSearchWithQueryText(ownProps.history, text))
    },
  }
}

const TopicsMenuContainer = withRouter(
  connect(null, mapDispatchToProps)(TopicsMenu)
)

export default TopicsMenuContainer
