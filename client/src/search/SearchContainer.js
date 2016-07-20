import { connect } from 'react-redux'
import SearchFacet from './SearchComponent'
import { triggerSearch, updateQuery } from './SearchActions'

const mapStateToProps = (state) => {
  return {
    searchText: state.get('search').get('text')
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => dispatch(triggerSearch()),
    updateQuery: (text) => dispatch(updateQuery(text)),
  }
}

const SearchFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFacet)

export default SearchFacetContainer
