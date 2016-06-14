import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { toggleVisibility } from './FacetActions'

const mapStateToProps = (state) => {
    return {
        opened: state.getIn(['facets', 'visible'])
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
      toggleVisibility: () => dispatch(toggleVisibility())
    };
};

const FacetsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(FacetList);

export default FacetsContainer