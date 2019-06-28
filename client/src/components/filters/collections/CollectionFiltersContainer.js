import {connect} from 'react-redux'
import CollectionFilters from './CollectionFilters'
import {closeLeft} from '../../../actions/LayoutActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    closeLeft: () => dispatch(closeLeft()),
  }
}

const CollectionFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionFilters)
)

export default CollectionFiltersContainer
