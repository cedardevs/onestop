import { connect } from 'react-redux'
import MapComponent from './MapComponent'
import { updateGeometry } from './MapActions'

const mapStateToProps = (state) => {
  return {
    currentGeometry: state.getIn(['search', 'index'])
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    handleGeometryUpdate: (text) => dispatch(updateGeometry(text))
  }
}

const SearchFacetContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(MapComponent)

export default MapContainer
