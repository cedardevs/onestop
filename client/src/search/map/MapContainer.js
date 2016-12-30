import { connect } from 'react-redux'
import MapComponent from './MapComponent'
import { newGeometry, removeGeometry } from './MapActions'

const mapStateToProps = (state) => {
  return {
    geoJsonSelection: state.map.geoJSON ? state.map.geoJSON : null
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleNewGeometry: geoJSON => dispatch(newGeometry(geoJSON)),
    removeGeometry: () => dispatch(removeGeometry())
  }
}

const MapContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(MapComponent)

export default MapContainer
