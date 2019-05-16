import {connect} from 'react-redux'
import Map from '../spatial/Map'

import {withRouter} from 'react-router'

// TODO: make new actions/reducers related to granules
// use CollectionMapContainer as reference point

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {}
}

const GranuleMapContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Map)
)

export default GranuleMapContainer
