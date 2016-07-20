import { connect } from 'react-redux'
import LandingComponent from './LandingComponent'
import { triggerSearch, updateQuery } from './SearchActions'

const mapStateToProps = (state) => {
  return {
    indexName: state.getIn(['search', 'index'])
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => dispatch(triggerSearch()),
    updateQuery: (text) => dispatch(updateQuery(text))
  }
}

const LandingContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LandingComponent)

export default LandingContainer
