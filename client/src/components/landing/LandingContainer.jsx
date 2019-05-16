import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Landing from './Landing'
import {
  triggerSearch,
  showCollections,
} from '../../actions/search/SearchActions'
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
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const LandingContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Landing)
)

export default LandingContainer
