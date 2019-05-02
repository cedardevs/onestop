import {connect} from 'react-redux'
import ArcGISMap from './ArcGISMap'
import {updateBounds} from '../../actions/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    showMap: state.ui.mapFilter.showMap,
    bounds: state.ui.mapFilter.bounds,
    boundsSource: state.ui.mapFilter.boundsSource,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    updateBounds: (bounds, source) => {
      dispatch(updateBounds(bounds, source))
    },
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    },
  }
}

const ArcGISMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(ArcGISMap)
)

export default ArcGISMapContainer
