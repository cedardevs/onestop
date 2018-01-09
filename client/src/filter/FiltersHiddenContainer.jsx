import {connect} from 'react-redux'
import FiltersHidden from './FiltersHidden'
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

const FiltersHiddenContainer = connect(mapStateToProps, mapDispatchToProps)(FiltersHidden)

export default FiltersHiddenContainer
