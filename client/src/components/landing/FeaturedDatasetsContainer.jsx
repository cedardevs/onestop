import {connect} from 'react-redux'
import FeaturedDatasets from './FeaturedDatasets'
import {collectionUpdateQueryText} from '../../actions/search/CollectionFilterActions'
import {
  asyncNewCollectionSearch,
  showCollections,
} from '../../actions/search/CollectionSearchActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    featured: state.config.featured,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(asyncNewCollectionSearch())
      dispatch(showCollections(ownProps.history))
    },
    collectionUpdateQueryText: text =>
      dispatch(collectionUpdateQueryText(text)),
  }
}

const FeaturedDatasetsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FeaturedDatasets)
)

export default FeaturedDatasetsContainer
