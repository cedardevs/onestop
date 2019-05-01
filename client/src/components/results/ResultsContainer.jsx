import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Results from './Results'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {}
}

const ResultsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Results)
)

export default ResultsContainer
