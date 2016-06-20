export const FETCH_DETAILS = 'fetch_details'
export const RECEIVE_DETAILS = 'receive_details'
export const FLIP_CARD = 'flip_card'

export const startDetails = (id) => {
  return {
    type: FETCH_DETAILS,
    id: id,
    flipped: false
  }
}

export const completeDetails = (id, details) => {
  return {
    type: RECEIVE_DETAILS,
    id: id,
    details: details,
    flipped: true
  }
}

export const flipCard = (id) => {
  return{
    type: FLIP_CARD,
    id: id
  }
}

export const getDetails = (id) => {
  return (dispatch, getState) => {
    // if a request is already in flight, let the calling code know there's nothing to wait for
    if (getState().getIn(['search', 'inFlight']) === true) {
      return Promise.resolve()
    }

    dispatch(startDetails(id))

    const details = getState().get('results').find(result => result.get('id') === id)
    console.log ("details in getDetails method is " + details)
    dispatch(completeDetails(id, details))
    dispatch(flipCard(id))
  }
}
