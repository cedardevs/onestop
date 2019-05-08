import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Landing from './Landing'
import {showCollections} from '../../actions/search/CollectionSearchActions'

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
  }
}

const LandingContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Landing)
)

export default LandingContainer
