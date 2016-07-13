import { connect } from 'react-redux'
import LandingComponent from './LandingComponent'
import { textSearch, indexChange } from './SearchActions'

const mapStateToProps = (state) => {
  return {
    indexName: state.getIn(['search', 'index'])
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: (text) => dispatch(textSearch(text)),
    handleIndexChange: (text) => dispatch(indexChange(text))
  }
}

const SearchFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LandingComponent)

export default SearchFacetContainer
