import {connect} from 'react-redux'
import Root from './Root'
import { toggleLeft, toggleRight } from '../actions/LayoutActions'

const mapStateToProps = state => {
  return {
    showLeft: state.ui.layout.showLeft,
    showRight: state.ui.layout.showRight
  }
}

const mapDispatchToProps = dispatch => {
  return {
    toggleLeft: () => dispatch(toggleLeft()),
    toggleRight: () => dispatch(toggleRight())
  }
}

const RootContainer = connect(mapStateToProps, mapDispatchToProps)(Root)

export default RootContainer
