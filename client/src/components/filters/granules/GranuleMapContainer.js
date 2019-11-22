import {connect} from 'react-redux'
import Map from '../spatial/Map'
import {
  granuleUpdateGeometry,
  granuleRemoveGeometry,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'
import {displayBboxAsMapGeometry} from '../../../utils/geoUtils'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {bbox} = state.search.granuleFilter
  return {
    filterType: 'granuleFilter',
    geoJsonSelection: displayBboxAsMapGeometry(bbox),
    showMap: state.layout.showMap,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: bbox => dispatch(granuleUpdateGeometry(bbox)),
    removeGeometry: () => dispatch(granuleRemoveGeometry()),
    submit: () => {
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
  }
}

const GranuleMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default GranuleMapContainer
