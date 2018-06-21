import {connect} from 'react-redux'
import Banner from './Banner'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return state.domain.config.banner ? state.domain.config.banner : {}
}

const BannerContainer = withRouter(connect(mapStateToProps)(Banner))

export default BannerContainer
