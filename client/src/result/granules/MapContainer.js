import { connect } from 'react-redux'
import MapComponent from '../../search/map/MapComponent'
import { toggleGranuleFocus } from './GranulesActions'
import L from 'leaflet'

const mapStateToProps = (state) => {
  let granules = state.getIn(['granules', 'granules'])
  let inFlight = state.getIn(['granules', 'inFlight'])
  let featureCollection = []
  if (!inFlight && granules.count()){
    granules.forEach((data, id)=> {
      featureCollection.push(convertToGeoJson(data.toJS()))
    })
  }
  return {
    geoJsonFeatures: featureCollection
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
  // Currently defaulting to rendering bounding box coordinates
  let geoJson = {
    geometry: {
      coordinates: bboxToCoords(recordData.spatialBounding.coordinates),
      type: "Polygon"
    },
    properties: recordData,
    type: "Feature"
  }
  return geoJson
}

const bboxToCoords = bbox => {
  const southWest = [bbox[0][0], bbox[0][1]]
  const southEast = [bbox[0][0], bbox[1][1]]
  const northEast = [bbox[1][0], bbox[1][1]]
  const northWest = [bbox[1][0], bbox[0][1]]
  return [[southWest, southEast, northEast, northWest]]
}

export default MapContainer
