import {connect} from 'react-redux'
import Disclaimer from './Disclaimer'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    disclaimer: state.config.disclaimer,
  }
}

const DisclaimerContainer = withRouter(connect(mapStateToProps)(Disclaimer))

export default DisclaimerContainer
