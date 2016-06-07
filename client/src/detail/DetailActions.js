export const FETCH_DETAILS = 'fetch_details';
export const RECEIVE_DETAILS = 'receive_details';
export const FLIP_CARD = 'flip_card';

export const startDetails = (id) => {
  return {
    type: FETCH_DETAILS,
    id: id,
    flipped: false
  };
};

export const completeDetails = (id, details) => {
  console.log("the id in completeDetails is " + id);
  console.log("this is a log");
  return {
    type: RECEIVE_DETAILS,
    id: id,
    details: details,
    flipped: false
  };
};

export const flipCard = (id, flipped ) => {
  return{
    type: FLIP_CARD,
    id: id,
    flipped: flipped
  }
};

export const getDetails = (id) => {
  return (dispatch, getState) => {
    // if a request is already in flight, let the calling code know there's nothing to wait for
    if (getState().getIn(['search', 'inFlight']) === true) {
      return Promise.resolve();
    }

    dispatch(startDetails(id));

    const details = getState().get('results').find(result => result.get('id') === id);
    console.log ("details in getDetails method is " + details);
    dispatch(completeDetails(id, details));
  };
};

export const toggleFlipCard = (id) => {
  return (dispatch , getState) =>
  {
    console.dir (getState().get('details'));

    const flipped = getState().get('details').find(result => result.get('flipped') === flipped);
    return dispatch(flipCard(id, flipped));
  }
};