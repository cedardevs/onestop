import {connect} from 'react-redux'
import InteractiveMap from './InteractiveMap'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {}
}

const InteractiveMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(InteractiveMap)
)

export default InteractiveMapContainer
