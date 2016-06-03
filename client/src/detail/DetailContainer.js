import { connect } from 'react-redux'
import { toggleFlipCard } from './DetailActions'
import DetailList from './DetailListComponent'

const mapStateToProps = (state) => {
  return {details: state.get('details').toJS()};
};

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(toggleFlipCard(id))
    }
  }
}

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(DetailList);

export default DetailContainer