import { connect } from 'react-redux'
import FooterComponent from './FooterComponent'

const mapStateToProps = (state) => {
    return {
        version: state.domain.info.version
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
    }
}

const FooterContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(FooterComponent)

export default FooterContainer
