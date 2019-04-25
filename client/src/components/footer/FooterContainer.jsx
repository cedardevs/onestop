import {connect} from 'react-redux'
import Footer from './Footer'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    version: state.search.info.version,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const FooterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Footer)
)

export default FooterContainer
