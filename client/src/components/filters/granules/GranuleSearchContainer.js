import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import GranuleSearch from './GranuleSearch'

import {
  granuleUpdateQuery,
} from '../../../actions/routing/GranuleSearchStateActions'
import {toggleMap} from '../../../actions/LayoutActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.granuleFilter.queryText,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: (text) => {
      dispatch(granuleUpdateQuery(text)) // TODO wrap into granule search params
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
      // TODO also probably need a clearQuery action
    },
  }
}

const GranuleSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleSearch)
)

export default GranuleSearchContainer
