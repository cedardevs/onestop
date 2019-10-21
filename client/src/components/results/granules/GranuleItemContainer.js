import {connect} from 'react-redux'
import {showGranuleVideo} from '../../../actions/LayoutActions'
import GranuleItem from './GranuleItem'

const mapStateToProps = state => {
  const {featuresEnabled} = state.config
  const {granuleVideo} = state.layout
  return {
    featuresEnabled: featuresEnabled,
    granuleVideoId: granuleVideo,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    showGranuleVideo: id => dispatch(showGranuleVideo(id)),
  }
}
const GranuleItemContainer = connect(mapStateToProps, mapDispatchToProps)(
  GranuleItem
)
export default GranuleItemContainer
