import { connect } from 'react-redux'
import { getDetails, CardStatus } from '../detail/DetailActions'
import Result from './ResultComponent'

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

const ResultContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Result)

export default ResultContainer
