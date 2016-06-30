import { connect } from 'react-redux'
import { getDetails, CardStatus } from './DetailActions'
import Detail from './DetailComponent'

const mapStateToProps = (state, ownProps) => {
  let cardDetails = state.getIn(['details',ownProps.recordId]).toJS()
  let cardStatus = cardDetails.cardStatus != CardStatus.SHOW_FRONT
  return {
    recordId: ownProps.recordId,
    title: cardDetails.title,
    thumbnail: cardDetails.thumbnail,
    description: cardDetails.description,
    flipped: cardStatus
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(getDetails(id))
    }
  }
}

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Detail)

export default DetailContainer
