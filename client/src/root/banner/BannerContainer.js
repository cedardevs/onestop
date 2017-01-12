import { connect } from 'react-redux'
import BannerComponent from './BannerComponent'

const mapStateToProps = (state) => {
  return state.config.banner ? state.config.banner : {}
}

const BannerContainer = connect(mapStateToProps)(BannerComponent)

export default BannerContainer
