import Map from '../spatial/Map'
import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {toggleMapClose} from '../../../actions/LayoutActions'
import {
  granuleUpdateGeometry,
  granuleRemoveGeometry,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    filterType: 'granuleFilter',
    bbox: state.search.granuleFilter.bbox,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    handleNewGeometry: bbox => dispatch(granuleUpdateGeometry(bbox)),
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
