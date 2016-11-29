import { connect } from 'react-redux'
import BannerComponent from './BannerComponent'

const mapStateToProps = (state) => {
  return state.getIn(['config', 'banner']) ? state.getIn(['config', 'banner']).toJS() : {}
}

const BannerContainer = connect(mapStateToProps)(BannerComponent)

export default BannerContainer