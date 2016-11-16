import { connect } from 'react-redux'
import MapComponent from '../../search/map/MapComponent'
import { toggleGranuleFocus } from './GranulesActions'

const mapStateToProps = (state) => {
  let granules = []
  let inFlight = state.getIn(['granules', 'inFlight'])
  if (!inFlight){
    granules = state.getIn(['granules', 'granules'])
    granules.forEach((data, id)=> {
      granules.update(id, convertToGeoJson(data.toJS()))
    })
  }
  return {
    geoJsonFeatures: granules ? granules.toJS() : []
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleGeometryFocus: id => toggleGranuleFocus(id)
  }
}

const MapContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(MapComponent)

const convertToGeoJson = recordData => {
  return {
    type: "Feature",
    properties: {},
    geometry: recordData.spatialBounding.coordinates
  }
}

export default MapContainer
