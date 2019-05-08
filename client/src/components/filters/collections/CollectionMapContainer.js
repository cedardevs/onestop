import {connect} from 'react-redux'
import Map from '../spatial/Map'
import {
  collectionUpdateGeometry,
  collectionRemoveGeometry,
} from '../../../actions/search/CollectionFilterActions'
import {collectionClearResults} from '../../../actions/search/CollectionResultActions'
import {
  triggerCollectionSearch,
  showCollections,
} from '../../../actions/search/CollectionSearchActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {geoJSON} = state.search.collectionFilter
  return {
    geoJsonSelection: geoJSON,
    showMap: state.layout.showMap,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: geoJSON => dispatch(collectionUpdateGeometry(geoJSON)),
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
    submit: () => {
      dispatch(collectionClearResults())
      dispatch(triggerCollectionSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const CollectionMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default CollectionMapContainer
