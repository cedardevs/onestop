import { connect } from 'react-redux'
import SpatialFilter from './SpatialFilter'
import { toggleExcludeGlobal } from '../actions/SearchParamActions'

const mapStateToProps = (state) => {
  return {
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    }
  }
}

const SpatialFilterContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(SpatialFilter)

export default SpatialFilterContainer