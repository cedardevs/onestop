import { connect } from 'react-redux'
import _ from 'lodash'
import MapComponent from '../../search/map/MapComponent'
import { toggleGranuleFocus } from '../../actions/FlowActions'
import { convertEnvelopeToPolygon } from '../../utils/geoUtils'

const mapStateToProps = (state) => {
  let { granules } = state.domain.results
  let featureCollection = []
  _.forOwn(granules, (data, id) => {
    featureCollection.push(convertToGeoJson(data, id))
  })
  return {
    geoJsonFeatures: featureCollection,
    focusedFeatures: state.ui.granuleDetails.focusedGranules
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

const convertToGeoJson = (recordData, id) => {
  // Currently defaulting to rendering bounding box coordinates
  return {
    geometry: convertEnvelopeToPolygon(recordData.spatialBounding),
    properties: Object.assign({}, recordData, {id: id}),
    type: "Feature"
  }
}

export default MapContainer
