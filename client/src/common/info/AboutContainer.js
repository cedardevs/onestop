import { connect } from 'react-redux'
import About from './About'

const mapStateToProps = state => {
  return {
    collectionsCount: state.domain.info.collectionsCount,
    granulesCount: state.domain.info.granulesCount,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const AboutContainer = connect(mapStateToProps, mapDispatchToProps)(About)

export default AboutContainer
