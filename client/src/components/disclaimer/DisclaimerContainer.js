import {connect} from 'react-redux'
import Disclaimer from './Disclaimer'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return state.domain.config.disclaimer ? state.domain.config.disclaimer : {}
}

const DisclaimerContainer = withRouter(connect(mapStateToProps)(Disclaimer))

export default DisclaimerContainer
