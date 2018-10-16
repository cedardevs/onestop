import {connect} from 'react-redux'
import Root from './Root'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    showLeft: state.ui.layout.showLeft,
    leftOpen: state.ui.layout.leftOpen,
    showRight: state.ui.layout.showRight,
    featuresEnabled: state.domain.config.featuresEnabled,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const RootContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Root)
)

export default RootContainer
