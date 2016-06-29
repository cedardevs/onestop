import { connect } from 'react-redux'
import { getDetails, CardStatus } from './DetailActions'
import Detail from './DetailComponent'

const mapStateToProps = (state, ownProps) => {
  let details = state.get('details').toJS()
  let cardStatus = details[ownProps.recordId].cardStatus != CardStatus.SHOW_FRONT
  return {
    details: details,
    recordId: ownProps.recordId,
    title: ownProps.title,
    thumbnail: ownProps.thumbnail,
    description: ownProps.description,
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
