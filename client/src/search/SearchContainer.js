import { connect } from 'react-redux'
import SearchFacet from './SearchComponent'
import { textSearch, indexChange } from './SearchActions'

const mapStateToProps = (state) => {
  return {
    searchText: state.get('search').get('text')
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: (text) => dispatch(textSearch(text)),
  }
}

const SearchFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFacet)

export default SearchFacetContainer
