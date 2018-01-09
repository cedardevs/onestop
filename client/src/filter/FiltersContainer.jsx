import {connect} from 'react-redux'
import Filters from './Filters'
import { toggleLeft } from '../actions/LayoutActions'

const mapStateToProps = state => {
  return {
    showLeft: state.ui.layout.showLeft
  }
}

const mapDispatchToProps = dispatch => {
  return {
    toggleLeft: () => dispatch(toggleLeft())
  }
}

const FiltersContainer = connect(mapStateToProps, mapDispatchToProps)(Filters)

export default FiltersContainer
