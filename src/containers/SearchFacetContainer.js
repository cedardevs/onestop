import { connect } from 'react-redux'
import SearchFacet from '../components/SearchFacet'
import { textSearch, indexChange } from '../actions/search'

const mapStateToProps = (state) => {
  return {
    indexName: state.getIn(['search', 'index'])
  };
};

const mapDispatchToProps = (dispatch) => {
  return {
    submit: (text) => dispatch(textSearch(text)),
    handleIndexChange: (text) => dispatch(indexChange(text))
  };
};

const SearchFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFacet);

export default SearchFacetContainer