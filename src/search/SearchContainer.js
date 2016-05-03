import { connect } from 'react-redux'
import SearchFacet from './SearchComponent'
import { textSearch, indexChange } from './SearchActions'

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