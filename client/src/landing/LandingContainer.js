import { connect } from 'react-redux'
import LandingComponent from './LandingComponent'
import { triggerSearch, clearFacets } from '../actions/SearchRequestActions'
import { updateQuery } from '../actions/SearchParamActions'
import { showCollections } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.behavior.search.queryText.text,
    showAbout: state.ui.toggles.about,
    showHelp: state.ui.toggles.help,
    featured: state.domain.config.featured
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateQuery: (text) => dispatch(updateQuery(text))
  }
}

const LandingContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LandingComponent)

export default LandingContainer
