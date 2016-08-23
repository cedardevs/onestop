import { connect } from 'react-redux'
import FacetComponent from '../facet/FacetListComponent'
import { triggerSearch, updateQuery } from '../search/SearchActions'

const mapStateToProps = (state) => {
    return {
        queryString: state.getIn(['search', 'text'])
    }
}

const mapDispatchToProps = (dispatch) => {
    return {
        submit: () => dispatch(triggerSearch()),
        updateQuery: (text) => dispatch(updateQuery(text))
    }
}

const FacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(FacetComponent)

export default FacetContainer

