import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Landing from './Landing'

const mapStateToProps = state => {
  return {
    featured: state.config.featured,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {}
}

const LandingContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Landing)
)

export default LandingContainer
