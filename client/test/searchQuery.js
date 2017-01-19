import nock from 'nock'

export const searchQuery = (testingRoot, requestBody) => {
  nock(testingRoot)
      .post('/onestop/api/search', requestBody)
      .reply(200, {
        data: [
          {
            type: 'collection',
            id: '123ABC',
            attributes: {
              field0: 'field0',
              field1: 'field1'
            }
          },
          {
            type: 'collection',
            id: '789XYZ',
            attributes: {
              field0: 'field00',
              field1: 'field01'
            }
          }
        ],
        meta:
        {
          facets:{
            science: [
              {term: "land", count: 2}
            ]},
          total: 2,
          took: 100
        }
      })
}

export const errorsArray = [
  {
    status: '500',
    title: 'Sorry, something has gone wrong',
    detail: 'Looks like something isn\'t working on our end, please try again later',
  }
]

export const errorQuery = (testingRoot, requestBody) => {
  nock(testingRoot)
      .post('/onestop/api/search', requestBody)
      .reply(500, {
        errors: errorsArray,
        meta: {
          timestamp: new Date().time,
          request: 'uri:/onestop/api/search',
          parameters: requestBody
        }
      })
}

export default searchQuery
