import {connect} from 'react-redux'
import Filters from './Filters'
import {closeLeft} from '../actions/LayoutActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    closeLeft: () => dispatch(closeLeft()),
  }
}

const FiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Filters)
)

export default FiltersContainer
