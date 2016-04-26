import { connect } from 'react-redux'
import SearchFacet from '../components/SearchFacet'
import { textSearch, indexChange } from '../actions/search'

const mapStateToProps = (state) => {
  return {
    indexIndex: state.get('indexIndex')
  };
};

const mapDispatchToProps = (dispatch) => {
  return {
    submit: (text) => dispatch(textSearch(text)),
    handleIndexChange: (index, text) => dispatch(indexChange(index, text))
  };
};

const SearchFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFacet);

export default SearchFacetContainer