import {connect} from 'react-redux'
import Root from './Root'

const mapStateToProps = state => {
  return {
    showLeft: state.ui.layout.showLeft,
    leftOpen: state.ui.layout.leftOpen,
    showRight: state.ui.layout.showRight,
    onDetailPage: state.ui.layout.onDetailPage,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const RootContainer = connect(mapStateToProps, mapDispatchToProps)(Root)

export default RootContainer
