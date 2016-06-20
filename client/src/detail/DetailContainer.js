import { connect } from 'react-redux'
import { flipCard } from './DetailActions'
//import DetailList from './DetailListComponent'
import ResultsList from '../result/ResultListComponent'

const mapStateToProps = (state) => {
  return {
    details: state.get('details').toJS()
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(flipCard(id))
    }
  }
}

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ResultsList);

export default DetailContainer
