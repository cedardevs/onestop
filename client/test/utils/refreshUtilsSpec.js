// import '../specHelper'
// import * as refreshUtils from '../../src/utils/refreshUtils'
//
// describe('The refreshUtils "execute search" functionality', function () {
//   it('executes a collections search if the url contains app state', function () {
//     beforeEach(() => {
//       spyOn(refreshUtils, 'someNamedExport')
//       spyOn(exports, 'default');          // mock the default export
//     });
//       const result = refreshUtils.executeSearch()
//
//     result.should.deep.equal({
//       queryText: '',
//       geoJSON: null,
//       startDateTime: null,
//       endDateTime: null,
//       selectedFacets: {},
//       selectedIds: []
//     })
//   })
//
//   it('executes a collections & granules searches if the url contains app state ', function () {
//     const result = refreshUtils.executeSearch()
//
//     result.should.deep.equal({
//       queryText: '',
//       geoJSON: null,
//       startDateTime: null,
//       endDateTime: null,
//       selectedFacets: {},
//       selectedIds: []
//     })
//   })
//
//   it('executes no search if the url contains no app state', function () {
//     const result = refreshUtils.executeSearch()
//
//     result.should.deep.equal({
//       queryText: '',
//       geoJSON: null,
//       startDateTime: null,
//       endDateTime: null,
//       selectedFacets: {},
//       selectedIds: []
//     })
//   })
// })
//
// describe('The refreshUtils "initial state" functionality', function () {
//   it('initializes the state if the url contains app state', function () {
//     const result = refreshUtils.executeSearch()
//
//     result.should.deep.equal({
//       queryText: '',
//       geoJSON: null,
//       startDateTime: null,
//       endDateTime: null,
//       selectedFacets: {},
//       selectedIds: []
//     })
//   })
//
//   it('initializes only empty state if the url contains no app state', function () {
//     const result = refreshUtils.executeSearch()
//
//     result.should.deep.equal({
//       queryText: '',
//       geoJSON: null,
//       startDateTime: null,
//       endDateTime: null,
//       selectedFacets: {},
//       selectedIds: []
//     })
//   })
// })
