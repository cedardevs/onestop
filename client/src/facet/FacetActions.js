export const OPEN = 'open';
export const CLOSED = 'closed';

export const opened = () => {
    return {
        type: OPEN
    };
};

export const closed = () => {
    return {
        type: CLOSED
    };
};

export const toggleVisibility = () => {
  return (dispatch, getState) => {
    console.log("visible: "+getState().getIn(['facets', 'visible']));

    if (getState().getIn(['facets', 'visible']) === true) {
        dispatch(closed());
    }
    else {
        dispatch(opened());
    }
  }
};