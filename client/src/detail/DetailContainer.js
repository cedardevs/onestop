import { connect } from 'react-redux'
import {toggleFlipCard} from './DetailActions'
import Detail from './DetailComponent'

const mapStateToProps = (state) => {
  return state.get('details').toJS();
};

const mapDispatchToProps = (dispatch) => {

  return {
    onCardClick: (id) => {
      dispatch(toggleFlipCard(id))
    }
  };
};

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Detail);

export default DetailContainer