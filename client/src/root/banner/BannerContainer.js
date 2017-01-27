import { connect } from 'react-redux'
import BannerComponent from './BannerComponent'

const mapStateToProps = (state) => {
  return state.domain.config.banner ? state.domain.config.banner : {}
}

const BannerContainer = connect(mapStateToProps)(BannerComponent)

export default BannerContainer
