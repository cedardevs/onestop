import nock from 'nock'

const searchQuery = (testingRoot, requestBody) => {

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
            ]}
        }

      })
}

export default searchQuery
