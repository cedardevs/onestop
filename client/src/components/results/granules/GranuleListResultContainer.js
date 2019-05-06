import {connect} from 'react-redux'
import {showGranuleVideo} from '../../../actions/LayoutActions'
import GranuleListResult from './GranuleListResult'

const mapStateToProps = state => {
  const {granuleVideo} = state.layout
  return {
    granuleVideoId: granuleVideo,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    showGranuleVideo: id => dispatch(showGranuleVideo(id)),
  }
}
const GranuleListResultContainer = connect(mapStateToProps, mapDispatchToProps)(
  GranuleListResult
)
export default GranuleListResultContainer
