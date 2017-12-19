import {connect} from 'react-redux'
import Map from './Map'
import {newGeometry, removeGeometry} from '../../actions/SearchParamActions'

const mapStateToProps = state => {
  const {geoJSON} = state.behavior.search
  return {
    geoJsonSelection: geoJSON,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    handleNewGeometry: geoJSON => dispatch(newGeometry(geoJSON)),
    removeGeometry: () => dispatch(removeGeometry()),
  }
}

const MapContainer = connect(mapStateToProps, mapDispatchToProps)(Map)

export default MapContainer
