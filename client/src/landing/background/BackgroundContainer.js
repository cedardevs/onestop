import { connect } from 'react-redux'
import BackgroundComponent from './BackgroundComponent'

const mapStateToProps = (state) => {
  const { showImage } = state.ui.background
  return {
    showImage
  }
}

const BackgroundContainer = connect(
    mapStateToProps
)(BackgroundComponent)

export default BackgroundContainer
