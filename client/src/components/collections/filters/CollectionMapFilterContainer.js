import {connect} from 'react-redux'
import MapFilter from '../../common/filters/spatial/MapFilter'
import {
  toggleExcludeGlobal,
  newGeometry,
  removeGeometry,
} from '../../../actions/SearchParamActions'
import {toggleMap} from '../../../actions/LayoutActions'
import {
  clearCollections,
  triggerSearch,
} from '../../../actions/SearchRequestActions'
import {showCollections} from '../../../actions/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    showMap: state.ui.layout.showMap,
    geoJSON: state.behavior.search.geoJSON,
    excludeGlobal: state.behavior.search.excludeGlobal,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    },
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    toggleMap: () => {
      dispatch(toggleMap())
    },
    removeGeometry: () => dispatch(removeGeometry()),
    handleNewGeometry: geoJSON => dispatch(newGeometry(geoJSON)),
  }
}

const CollectionMapFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(MapFilter)
)

export default CollectionMapFilterContainer
