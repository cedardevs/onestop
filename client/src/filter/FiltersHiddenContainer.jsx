import {connect} from 'react-redux'
import FiltersHidden from './FiltersHidden'
import {openLeft} from '../actions/LayoutActions'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    openLeft: () => dispatch(openLeft()),
  }
}

const FiltersHiddenContainer = connect(mapStateToProps, mapDispatchToProps)(
  FiltersHidden
)

export default FiltersHiddenContainer
