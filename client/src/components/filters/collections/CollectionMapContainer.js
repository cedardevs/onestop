import {connect} from 'react-redux'
import {
  collectionRemoveGeometry,
  collectionUpdateGeometry,
} from '../../../actions/routing/CollectionSearchStateActions'
import {submitCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

import {withRouter} from 'react-router'
import Map from '../spatial/Map'
import {toggleMapClose} from '../../../actions/LayoutActions'

const mapStateToProps = state => {
  const {geoJSON} = state.search.collectionFilter
  return {
    filterType: 'collectionFilter',
    geoJsonSelection: geoJSON,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: geoJSON => dispatch(collectionUpdateGeometry(geoJSON)),
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
    submit: () => {
      dispatch(submitCollectionSearch(ownProps.history))
    },
    closeMap: () => {
      dispatch(toggleMapClose())
    },
  }
}

const CollectionMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default CollectionMapContainer
