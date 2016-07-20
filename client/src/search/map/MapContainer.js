import { connect } from 'react-redux'
import MapComponent from './MapComponent'
import { updateGeometry } from './MapActions'

const mapStateToProps = (state) => {
  return {
    geoJSON: state.getIn(['search', 'geoJSON'])
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleGeometryUpdate: (geoJSON) => dispatch(updateGeometry(geoJSON))
  }
}

const MapContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(MapComponent)

export default MapContainer
