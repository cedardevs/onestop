import {connect} from 'react-redux'
import _ from 'lodash'
import GranuleMap from './GranuleMap'
import {toggleGranuleFocus} from '../../actions/FlowActions'

import {ensureDatelineFriendlyGeometry} from '../../utils/geoUtils'

const mapStateToProps = state => {
  let {granules} = state.domain.results
  let featureCollection = []
  _.forOwn(granules, (data, id) => {
    if (data.spatialBounding) {
      featureCollection.push(convertToGeoJson(data, id))
    }
  })
  return {
    geoJsonFeatures: featureCollection,
    focusedFeatures: state.ui.granuleDetails.focusedGranules,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    toggleGeometryFocus: id => toggleGranuleFocus(id),
  }
}

const GranulesMapContainer = connect(mapStateToProps, mapDispatchToProps)(
  GranuleMap
)

const convertToGeoJson = (recordData, id) => {
  // Currently defaulting to rendering bounding box coordinates
  return {
    geometry: ensureDatelineFriendlyGeometry(recordData.spatialBounding),
    properties: _.assign({}, recordData, {id: id}),
    type: 'Feature',
  }
}

export default GranulesMapContainer
