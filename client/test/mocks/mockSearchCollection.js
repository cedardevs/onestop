export const mockSearchCollectionQuery = {
  queries: [
    {
      type: 'queryText',
      value: 'alaska',
    },
  ],
  filters: [],
  facets: true,
  page: {
    max: 20,
    offset: 0,
  },
}

export const mockSearchCollectionResponse = {
  data: [
    {
      type: 'collection',
      id: '123ABC',
      attributes: {
        field0: 'field0',
        field1: 'field1',
      },
    },
    {
      type: 'collection',
      id: '789XYZ',
      attributes: {
        field0: 'field00',
        field1: 'field01',
      },
    },
  ],
  meta: {
    facets: {
      science: [ {term: 'land', count: 2} ],
    },
    total: 2,
    took: 100,
  },
}

export const collectionErrorsArray = [
  {
    status: '500',
    title: 'Sorry, something has gone wrong',
    detail:
      "Looks like something isn't working on our end, please try again later",
  },
]

export const mockSearchCollectionErrorResponse = {
  errors: collectionErrorsArray,
  meta: {
    timestamp: new Date().time,
    request: 'uri:/onestop/api/search',
    parameters: mockSearchCollectionQuery,
  },
}
