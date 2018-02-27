import {connect} from 'react-redux'
import Filters from './Filters'
import {closeLeft} from '../actions/LayoutActions'

const mapStateToProps = state => {
  return {
    // leftOpen: state.ui.layout.leftOpen,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    closeLeft: () => dispatch(closeLeft()),
  }
}

const FiltersContainer = connect(mapStateToProps, mapDispatchToProps)(Filters)

export default FiltersContainer
