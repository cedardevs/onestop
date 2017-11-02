import { connect } from 'react-redux'
import Info from './Info'
import { toggleHelp, toggleAbout } from  '../../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
    showAbout: state.ui.toggles.about,
    showHelp: state.ui.toggles.help
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleAbout: () => dispatch(toggleAbout()),
    toggleHelp: () => dispatch(toggleHelp())
  }
}

const InfoContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Info)

export default InfoContainer
