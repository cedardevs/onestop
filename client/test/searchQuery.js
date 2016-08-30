import nock from 'nock'

const searchQuery = (testingRoot, requestBody) => {

  // nock.disableNetConnect()
  //
  // const testingRoot = 'http://localhost:9090'
  const expectedFacets = new Map()
  expectedFacets.set("facets", {science: [{term: "Land Surface", count: 2}, {term: "Land Surface > Topography", count: 2}]})

  nock(testingRoot)
      .post('/api/search', requestBody)
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
        meta: expectedFacets

      })
}

export default searchQuery
