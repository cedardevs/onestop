import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Root from './Root'

const mapStateToProps = state => {
  return {
    leftOpen: state.layout.leftOpen,
    rightOpen: state.layout.rightOpen,
    showMap: state.layout.showMap,
  }
}

const RootContainer = withRouter(connect(mapStateToProps, null)(Root))

export default RootContainer
