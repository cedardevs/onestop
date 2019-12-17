import {connect} from 'react-redux'
import MapFxn from '../spatial/MapFxn'
import {
  granuleUpdateGeometry,
  granuleRemoveGeometry,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

import {withRouter} from 'react-router'

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
  }
}

const GranuleMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(MapFxn)
)

export default GranuleMapContainer
