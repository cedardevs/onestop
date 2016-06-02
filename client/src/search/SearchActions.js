import fetch from 'isomorphic-fetch'

export const INDEX_CHANGE = 'index_change';
export const SEARCH = 'search';
export const SEARCH_COMPLETE = 'search_complete';

export const startSearch = (searchText) => {
  return {
    type: SEARCH,
    searchText
  };
};

export const completeSearch = (searchText, items) => {
  return {
    type: SEARCH_COMPLETE,
    searchText,
    items
  };
};

export const textSearch = (searchText) => {
  return (dispatch, getState) => {
    // if a search is already in flight, let the calling code know there's nothing to wait for
    if (getState().getIn(['search', 'inFlight']) === true) {
      return Promise.resolve();
    }

    dispatch(startSearch(searchText));

    const index = getState().getIn(['search', 'index']);
    console.log(`Searching: searchText="${index}":"${searchText}`);

    const apiRoot = "http://localhost:8000/onestop/search";
    const fetchParams = {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        searchText: '${searchText}'
      })
    };

    return fetch(apiRoot, fetchParams)
        .then(response => response.json())
        .then(json => dispatch(completeSearch(searchText, json.items)));

  };
};

export const indexChange = (indexText) => {
  return {
    type: INDEX_CHANGE,
    indexText
  };
};