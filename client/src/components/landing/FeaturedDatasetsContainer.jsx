import {connect} from 'react-redux'
import FeaturedDatasets from './FeaturedDatasets'
import {
  triggerSearch,
  clearFacets,
} from '../../actions/search/collections/SearchRequestActions'
import {updateQuery} from '../../actions/search/collections/SearchParamActions'
import {showCollections} from '../../actions/search/collections/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    featured: state.domain.config.featured,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    updateQuery: text => dispatch(updateQuery(text)),
  }
}

const FeaturedDatasetsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FeaturedDatasets)
)

export default FeaturedDatasetsContainer
