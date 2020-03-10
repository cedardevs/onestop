import _ from 'lodash'
import {withRouter} from 'react-router'
import {connect} from 'react-redux'
import GranuleFilters from './GranuleFilters'
import {closeLeft} from '../../../actions/LayoutActions'
import {
  clearGranuleQueryText,
  setGranuleQueryText,
  granuleToggleAllTermsMustMatch,
  resetGranuleAllTermsMustMatch,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.granuleFilter.title,
    allTermsMustMatch: state.search.granuleFilter.allTermsMustMatch,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    closeLeft: () => dispatch(closeLeft()),
    clear: () => {
      dispatch(clearGranuleQueryText())
      dispatch(resetGranuleAllTermsMustMatch())
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
    submit: text => {
      dispatch(setGranuleQueryText(text))
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
    toggleAllTermsMustMatch: text => {
      dispatch(granuleToggleAllTermsMustMatch())
      if (!_.isEmpty(text)) {
        // this is just to keep it from reapplying the search when no query has been applied
        dispatch(
          submitGranuleSearch(ownProps.history, ownProps.match.params.id)
        )
      }
    },
  }
}

const GranuleFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleFilters)
)

export default GranuleFiltersContainer
