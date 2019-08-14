import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import GranuleSearch from './GranuleSearch'

import {granuleUpdateQuery} from '../../../actions/routing/GranuleSearchStateActions'
import {toggleMap} from '../../../actions/LayoutActions'
import {submitGranuleSearchWithFilter} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    queryString: state.search.granuleFilter.queryText,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    clear: () => {
      // TODO also probably need a clearQuery action
      dispatch(
        submitGranuleSearchWithFilter(
          ownProps.history,
          ownProps.match.params.id,
          {queryText: ''}
        )
      )
    },
    submit: text => {
      dispatch(
        submitGranuleSearchWithFilter(
          ownProps.history,
          ownProps.match.params.id,
          {queryText: text}
        )
      )
    },
  }
}

const GranuleSearchContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleSearch)
)

export default GranuleSearchContainer
