import { connect } from 'react-redux'
import Filter from './Filter'
import { clearCollections, triggerSearch } from '../actions/SearchRequestActions'
import { showCollections } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    }
  }
}

const FilterContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Filter)

export default FilterContainer
