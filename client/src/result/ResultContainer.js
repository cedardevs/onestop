import { connect } from 'react-redux'
import ResultLayout from './ResultLayout'

const mapStateToProps = (state) => {
    return {
        selectedFacets: state.behavior.search.selectedFacets
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
    }
}

const ResultContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ResultLayout)

export default ResultContainer