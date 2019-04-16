import {connect} from 'react-redux'
import MapFilter from '../spatial/MapFilter'
import {
  toggleExcludeGlobal,
  newGeometry,
  removeGeometry,
} from '../../../actions/search/collections/SearchParamActions'
import {toggleMap} from '../../../actions/layout/LayoutActions'
import {
  clearCollections,
  triggerSearch,
} from '../../../actions/search/collections/SearchRequestActions'
import {showCollections} from '../../../actions/search/collections/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    showMap: state.layout.showMap,
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
