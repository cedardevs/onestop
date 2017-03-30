import { connect } from 'react-redux'
import InfoComponent from './InfoComponent'

const mapStateToProps = (state) => {
  return {
    showAbout: state.ui.toggles.about,
    showHelp: state.ui.toggles.help
  }
}

const InfoContainer = connect(
    mapStateToProps
)(InfoComponent)

export default InfoContainer
