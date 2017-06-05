import { connect } from 'react-redux'
import ResultLayout from './ResultLayout'
import {toggleFacet} from '../actions/SearchParamActions'
import {clearCollections, triggerSearch} from '../actions/SearchRequestActions'
import {showCollections} from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
    selectedFacets: state.behavior.search.selectedFacets
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFacet: (category, facetName, selected) =>
      dispatch(toggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    }
  }
}

const ResultContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(ResultLayout)

export default ResultContainer