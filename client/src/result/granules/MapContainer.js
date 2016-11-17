import { connect } from 'react-redux'
import MapComponent from '../../search/map/MapComponent'
import { toggleGranuleFocus } from './GranulesActions'

const mapStateToProps = (state) => {
  let granules = state.getIn(['granules', 'granules'])
  let inFlight = state.getIn(['granules', 'inFlight'])
  let granulesMap = new Map()
  if (!inFlight && granules.count()){
    granules.forEach((data, id)=> {
      granulesMap.set(id, convertToGeoJson(data.toJS()))
    })
  }
  return {
    geoJsonFeatures: granulesMap
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
