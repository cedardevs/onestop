import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Landing from './Landing'
import {
  triggerCollectionSearch,
  showCollections,
} from '../../actions/search/CollectionSearchActions'
import {collectionClearFacets} from '../../actions/search/CollectionFilterActions'

const mapStateToProps = state => {
  return {
    featured: state.config.featured,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    submit: () => {
      dispatch(collectionClearFacets())
      dispatch(triggerCollectionSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const LandingContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Landing)
)

export default LandingContainer
