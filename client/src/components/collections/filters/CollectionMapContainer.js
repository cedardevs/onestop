import {connect} from 'react-redux'
import Map from '../../common/filters/spatial/Map'
import {newGeometry, removeGeometry} from '../../../actions/SearchParamActions'
import {
  clearCollections,
  triggerSearch,
} from '../../../actions/SearchRequestActions'
import {showCollections} from '../../../actions/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {geoJSON} = state.behavior.search
  return {
    geoJsonSelection: geoJSON,
    showMap: state.ui.layout.showMap,
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
