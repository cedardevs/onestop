import {connect} from 'react-redux'
import Map from '../spatial/Map'
import {
  collectionUpdateGeometry,
  collectionRemoveGeometry,
} from '../../../actions/routing/CollectionSearchStateActions'
import {submitCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    filterType: 'collectionFilter',
    bbox: state.search.collectionFilter.bbox,
    showMap: state.layout.showMap,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: bbox => dispatch(collectionUpdateGeometry(bbox)),
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
    submit: () => {
      dispatch(submitCollectionSearch(ownProps.history))
    },
  }
}

const CollectionMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default CollectionMapContainer
