export const FETCH_DETAILS = 'fetch_details';
export const RECEIVE_DETAILS = 'receive_details';

export const startDetails = (id) => {
  return {
    type: FETCH_DETAILS,
    id: id
  };
};

export const completeDetails = (id, details) => {
  return {
    type: RECEIVE_DETAILS,
    id: id,
    details: details
  };
};

export const getDetails = (id) => {
  return (dispatch, getState) => {
    // if a request is already in flight, let the calling code know there's nothing to wait for
    if (getState().get('inFlight') === true) {
      return Promise.resolve();
    }

    dispatch(startDetails(id));

    const details = getState().get('results').find(result => result.get('id') === id);
    dispatch(completeDetails(id, details));
  };
};