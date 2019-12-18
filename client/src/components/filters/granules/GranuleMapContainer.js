import {connect} from 'react-redux'
import Map from '../spatial/Map'
import {
  granuleUpdateGeometry,
  granuleRemoveGeometry,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

import {withRouter} from 'react-router'
import {toggleMapClose} from '../../../actions/LayoutActions'

const mapStateToProps = state => {
  const {geoJSON} = state.search.granuleFilter
  return {
    filterType: 'granuleFilter',
    geoJsonSelection: geoJSON,
    showMap: state.layout.showMap,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: geoJSON => dispatch(granuleUpdateGeometry(geoJSON)),
    removeGeometry: () => dispatch(granuleRemoveGeometry()),
    submit: () => {
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
    closeMap: () => {
      dispatch(toggleMapClose())
    },
  }
}

const GranuleMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default GranuleMapContainer
