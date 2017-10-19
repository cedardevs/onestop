import { connect } from 'react-redux'
import BackgroundComponent from './BackgroundComponent'

const mapStateToProps = (state) => {
  return {}
}

const BackgroundContainer = connect(
    mapStateToProps
)(BackgroundComponent)

export default BackgroundContainer



// TODO DELETE THIS CONTAINER IT DOES NOTHING
