import { connect } from 'react-redux'
import { toggleGranuleFocus } from '../GranulesActions'
import GranuleList from './GranuleListComponent'

const mapStateToProps = (state) => {
  const id = state.getIn(['collections', 'selectedIds']).first()
  return {
    results: state.getIn(['granules', 'granules']),
    focusedIds: state.getIn(['granules', 'focusedGranules']),
    selectedCollection: state.getIn(['collections', 'results', id]) ?
      state.getIn(['collections', 'results', id]).toJS() : {}
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onMouseOver: (id) => {
      dispatch(toggleGranuleFocus(id))
    }
  }
}

const GranuleListContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(GranuleList)

export default GranuleListContainer
