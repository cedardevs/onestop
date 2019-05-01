import {connect} from 'react-redux'
import FacetTree from './FacetTree'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    loading: state.ui.loading,
  }
}

const FacetTreeContainer = withRouter(connect(mapStateToProps)(FacetTree))

export default FacetTreeContainer
