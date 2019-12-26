import Map from '../spatial/Map'
import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {toggleMapClose} from '../../../actions/LayoutActions'
import {submitCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'
import {
  collectionUpdateGeometry,
  collectionRemoveGeometry,
} from '../../../actions/routing/CollectionSearchStateActions'

const mapStateToProps = state => {
  return {
    filterType: 'collectionFilter',
    bbox: state.search.collectionFilter.bbox,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: bbox => dispatch(collectionUpdateGeometry(bbox)),
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
