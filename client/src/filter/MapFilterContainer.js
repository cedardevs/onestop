import { connect } from 'react-redux'
import MapFilter from './MapFilter'
import { toggleExcludeGlobal } from '../actions/SearchParamActions'
import { toggleMap, updateBounds } from '../actions/FlowActions'

const mapStateToProps = state => {
  return {
    showMap: state.ui.mapFilter.showMap,
    bounds: state.ui.mapFilter.bounds,
    boundsSource: state.ui.mapFilter.boundsSource,
    geoJSON: state.behavior.search.geoJSON
  }
}

const mapDispatchToProps = dispatch => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    },
    toggleMap: () => {
      dispatch(toggleMap())
    },
    updateBounds: (bounds, source) => {
      dispatch(updateBounds(bounds, source))
    }
  }
}

const MapFilterContainer = connect(mapStateToProps, mapDispatchToProps)(MapFilter)

export default MapFilterContainer