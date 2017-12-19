import {connect} from 'react-redux'
import Banner from './Banner'

const mapStateToProps = state => {
  return state.domain.config.banner ? state.domain.config.banner : {}
}

const BannerContainer = connect(mapStateToProps)(Banner)

export default BannerContainer
