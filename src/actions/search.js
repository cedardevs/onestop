export const SEARCH = 'search';

export const textSearch = (searchText) => {
  return {
    type: SEARCH,
    params: {text: searchText}
  };
};