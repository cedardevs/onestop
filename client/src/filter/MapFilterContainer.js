import { connect } from 'react-redux'
import SpatialFilter from './MapFilter'
import { toggleExcludeGlobal } from '../actions/SearchParamActions'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    },
  }
}

const MapFilterContainer = connect(mapStateToProps, mapDispatchToProps)(
  SpatialFilter
)

export default MapFilterContainer
