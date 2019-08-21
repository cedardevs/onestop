import {withRouter} from 'react-router'
import {connect} from 'react-redux'
import GranuleFilters from './GranuleFilters'
import {closeLeft} from '../../../actions/LayoutActions'
import {
  clearGranuleQueryText,
  setGranuleQueryText,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.granuleFilter.title,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    closeLeft: () => dispatch(closeLeft()),
    clear: () => {
      dispatch(clearGranuleQueryText())
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
    submit: text => {
      dispatch(setGranuleQueryText(text))
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
  }
}

const GranuleFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleFilters)
)

export default GranuleFiltersContainer
