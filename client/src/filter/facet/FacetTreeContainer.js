import { connect } from 'react-redux'
import FacetTree from './FacetTree'

const mapStateToProps = (state) => {
  return {
    loading: state.ui.loading
  }
}

const FacetTreeContainer = connect(
    mapStateToProps
)(FacetTree)

export default FacetTreeContainer
