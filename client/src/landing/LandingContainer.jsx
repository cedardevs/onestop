import {connect} from 'react-redux'
import Landing from './Landing'
import {triggerSearch, clearFacets} from '../actions/SearchRequestActions'
import {updateQuery} from '../actions/SearchParamActions'
import {showCollections} from '../actions/FlowActions'

const mapStateToProps = state => {
  return {
    featured: state.domain.config.featured,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateQuery: text => dispatch(updateQuery(text)),
  }
}

const LandingContainer = connect(mapStateToProps, mapDispatchToProps)(Landing)

export default LandingContainer
