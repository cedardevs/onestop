import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Filters from './Filters'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {}
}

const FiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Filters)
)

export default FiltersContainer
