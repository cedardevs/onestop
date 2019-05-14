import {connect} from 'react-redux'
import FeaturedDatasets from './FeaturedDatasets'
import {collectionUpdateQueryText} from '../../actions/routing/CollectionSearchStateActions'
import {asyncNewCollectionSearch} from '../../actions/routing/CollectionSearchRouteActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    featured: state.config.featured,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(asyncNewCollectionSearch(ownProps.history))
    },
    collectionUpdateQueryText: text =>
      dispatch(collectionUpdateQueryText(text)),
  }
}

const FeaturedDatasetsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FeaturedDatasets)
)

export default FeaturedDatasetsContainer
