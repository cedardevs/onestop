import { connect } from 'react-redux'
import LandingComponent from './LandingComponent'
import { triggerSearch, updateQuery } from '../search/SearchActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.getIn(['search', 'text'])
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
