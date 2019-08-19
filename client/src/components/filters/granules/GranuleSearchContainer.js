import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import GranuleSearch from './GranuleSearch'

import {
  clearGranuleQueryText,
  setGranuleQueryText,
} from '../../../actions/routing/GranuleSearchStateActions'
import {toggleMap} from '../../../actions/LayoutActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.granuleFilter.title,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    clear: () => {
      dispatch(clearGranuleQueryText())
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))

      // dispatch(
      //   submitGranuleSearchWithFilter(
      //     ownProps.history,
      //     ownProps.match.params.id,
      //     {queryText: ''}
      //   )
      // )
    },
    submit: text => {
      dispatch(setGranuleQueryText(text))
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
      // dispatch(
      //   submitGranuleSearchWithFilter(
      //     ownProps.history,
      //     ownProps.match.params.id,
      //     {queryText: text}
      //   )
      // )
    },
  }
}

const GranuleSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleSearch)
)

export default GranuleSearchContainer
