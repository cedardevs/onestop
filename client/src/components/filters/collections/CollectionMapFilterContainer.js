import {connect} from 'react-redux'
import MapFilter from '../spatial/MapFilter'
import {
  collectionToggleExcludeGlobal,
  collectionUpdateGeometry,
  collectionRemoveGeometry,
} from '../../../actions/search/CollectionFilterActions'
import {toggleMap} from '../../../actions/LayoutActions'
import {collectionClearResults} from '../../../actions/search/CollectionResultActions'
import {
  triggerSearch,
  showCollections,
} from '../../../actions/search/CollectionSearchActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    showMap: state.layout.showMap,
    geoJSON: state.search.collectionFilter.geoJSON,
    excludeGlobal: state.search.collectionFilter.excludeGlobal,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(collectionToggleExcludeGlobal())
    },
    submit: () => {
      dispatch(collectionClearResults())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    toggleMap: () => {
      dispatch(toggleMap())
    },
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
    handleNewGeometry: geoJSON => dispatch(collectionUpdateGeometry(geoJSON)),
  }
}

const CollectionMapFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(MapFilter)
)

export default CollectionMapFilterContainer
