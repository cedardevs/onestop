import {connect} from 'react-redux'
import Map from '../spatial/Map'
import {
  newGeometry,
  removeGeometry,
} from '../../../actions/search/collections/SearchParamActions'
import {
  clearCollections,
  triggerSearch,
} from '../../../actions/search/collections/SearchRequestActions'
import {showCollections} from '../../../actions/search/collections/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {geoJSON} = state.behavior.search
  return {
    geoJsonSelection: geoJSON,
    showMap: state.layout.showMap,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: geoJSON => dispatch(newGeometry(geoJSON)),
    removeGeometry: () => dispatch(removeGeometry()),
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const CollectionMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default CollectionMapContainer
