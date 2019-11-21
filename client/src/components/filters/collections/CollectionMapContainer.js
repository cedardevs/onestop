import {connect} from 'react-redux'
import {
  collectionRemoveGeometry,
  collectionUpdateGeometry,
} from '../../../actions/routing/CollectionSearchStateActions'
import {submitCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

import {withRouter} from 'react-router'
import MapFxn from '../spatial/MapFxn'

const mapStateToProps = state => {
  const {geoJSON} = state.search.collectionFilter
  return {
    filterType: 'collectionFilter',
    geoJsonSelection: geoJSON,
    open: state.layout.showMap,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: geoJSON => dispatch(collectionUpdateGeometry(geoJSON)),
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
    submit: () => {
      dispatch(submitCollectionSearch(ownProps.history))
    },
  }
}

const CollectionMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(MapFxn)
)

export default CollectionMapContainer
