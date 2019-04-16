import {connect} from 'react-redux'
import Root from './Root'
import {withRouter} from 'react-router'
import {closeRight, openRight} from '../../actions/layout/LayoutActions'

const mapStateToProps = state => {
  return {
    leftOpen: state.layout.leftOpen,
    rightOpen: state.layout.rightOpen,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    closeRight: () => dispatch(closeRight()),
    openRight: () => dispatch(openRight()),
  }
}

const RootContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Root)
)

export default RootContainer
