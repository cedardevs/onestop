import {connect} from 'react-redux'
import Root from './Root'

const mapStateToProps = state => {
  return {
    showLeft: state.ui.layout.showLeft,
    leftOpen: state.ui.layout.leftOpen,
    showRight: state.ui.layout.showRight,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const RootContainer = connect(mapStateToProps, mapDispatchToProps)(Root)

export default RootContainer
