import {connect} from 'react-redux'
import GranuleFilters from './GranuleFilters'
import {closeLeft} from '../../../actions/layout/LayoutActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    closeLeft: () => dispatch(closeLeft()),
  }
}

const GranuleFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranuleFilters)
)

export default GranuleFiltersContainer
