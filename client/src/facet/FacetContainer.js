import { connect } from 'react-redux'
import FacetComponent from './FacetListComponent'

const mapStateToProps = (state) => {
    return {
        queryString: console.log(state.getIn(['search', 'text']))
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
        onChange: () => dispatch(console.log('facets')),
    }
}

const FacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(FacetComponent)

export default FacetContainer

