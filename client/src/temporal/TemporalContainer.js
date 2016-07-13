import { connect } from 'react-redux'
import SearchFacet from './search/SearchComponent'
import { temporalSearch, indexChange } from '../temporal/TemporalAction'

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

const TemporalFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(SearchFacet)

export default TemporalFacetContainer
