import { connect } from 'react-redux'
import SearchFieldsComponent from './SearchFieldsComponent'
import { triggerSearch, updateQuery } from './SearchActions'
import { clearFacets } from './facet/FacetActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.getIn(['search', 'text'])
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
    },
    updateQuery: (text) => dispatch(updateQuery(text))
  }
}

const SearchFieldsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFieldsComponent)

export default SearchFieldsContainer
