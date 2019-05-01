import {connect} from 'react-redux'
import FiltersHidden from './FiltersHidden'
import {openLeft} from '../../actions/LayoutActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    openLeft: () => dispatch(openLeft()),
  }
}

const FiltersHiddenContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FiltersHidden)
)

export default FiltersHiddenContainer
