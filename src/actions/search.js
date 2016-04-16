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
    if (getState().get('inFlight') === true) {
      return Promise.resolve();
    }

    dispatch(startSearch(searchText));

    // simulate calling a server
    setTimeout(() => {
      dispatch(completeSearch(searchText, [
        {name: 'Test result 1', thumbnail: '//blog.cloudera.com/wp-content/uploads/2015/12/docker-logo.png'},
        {name: 'Test result 2', thumbnail: '//blog.cloudera.com/wp-content/uploads/2015/12/docker-logo.png'},
        {name: 'Test result 3', thumbnail: '//blog.cloudera.com/wp-content/uploads/2015/12/docker-logo.png'},
        {name: 'Test result 4', thumbnail: '//blog.cloudera.com/wp-content/uploads/2015/12/docker-logo.png'},
        {name: 'Test result 5', thumbnail: '//blog.cloudera.com/wp-content/uploads/2015/12/docker-logo.png'},
        {name: 'Test result 6', thumbnail: '//blog.cloudera.com/wp-content/uploads/2015/12/docker-logo.png'}
      ]))
    }, 1000);
  };
};