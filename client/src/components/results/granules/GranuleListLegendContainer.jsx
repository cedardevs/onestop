import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import GranuleListLegend from './GranuleListLegend'

const mapStateToProps = state => {
  const {granules} = state.search.granuleResult

  return {
    results: granules,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const GranuleListLegendContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleListLegend)
)

export default GranuleListLegendContainer
