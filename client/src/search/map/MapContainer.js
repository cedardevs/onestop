import {connect} from 'react-redux'
import Map from './Map'
import {newGeometry, removeGeometry} from '../../actions/SearchParamActions'
import {
  clearCollections,
  triggerSearch,
} from '../../actions/SearchRequestActions'
import {showCollections} from '../../actions/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {geoJSON} = state.behavior.search
  return {
    geoJsonSelection: geoJSON,
    showMap: state.ui.layout.showMap,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    handleNewGeometry: geoJSON => dispatch(newGeometry(geoJSON)),
    removeGeometry: () => dispatch(removeGeometry()),
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
  }
}

const MapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default MapContainer
