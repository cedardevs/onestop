import {connect} from 'react-redux'
import Footer from './Footer'

const mapStateToProps = state => {
  return {
    version: state.domain.info.version,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const FooterContainer = connect(mapStateToProps, mapDispatchToProps)(Footer)

export default FooterContainer
