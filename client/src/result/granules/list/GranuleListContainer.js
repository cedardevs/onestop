import { connect } from 'react-redux'
import { toggleGranuleFocus } from '../GranulesActions'
import GranuleList from './GranuleListComponent'

const mapStateToProps = (state) => {
    return {
        results: state.getIn(['granules', 'granules']),
        focusedIds: state.getIn(['granules', 'focusedGranules'])
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
        onMouseOver: (id) => {
            dispatch(toggleGranuleFocus(id))
        }
    }
}

const GranuleListContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(GranuleList)

export default GranuleListContainer