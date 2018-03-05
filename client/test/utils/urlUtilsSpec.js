import '../specHelper'
import * as urlUtils from '../../src/utils/urlUtils'
import { expect } from 'chai'

describe('The URL Utils', function () {
  const pathTests = [
    {path: 'collections/details/foobar', isDetail: true, isGranuleList: false, collectionIdFromDetailPath: 'foobar'},
    {path: 'collections?q=foobar', isDetail: false, isGranuleList: false},
    {path: 'collections/granules/foobar?q=test', isDetail: false, isGranuleList: true, collectionIdFromGranuleListPath: 'foobar'},
  ]

  it('identify external urls', function () {
    const urls = [
        '//www.google.com',
        'http://www.google.com',
        'https://www.google.com',
        'ftp://www.google.com',
        'ftps://www.google.com'
    ]

    urls.forEach((url) => urlUtils.isGovExternal(url).should.equal(true, `Failed for url: ${url}`))
  })

  it('identify internal urls', function () {
    const urls = [
        '/search',
        '//www.ngdc.noaa.gov',
        'http://www.ngdc.noaa.gov',
        'https://www.ngdc.noaa.gov'
    ]

    urls.forEach((url) => urlUtils.isGovExternal(url).should.equal(false, `Failed for url: ${url}`))
  })

  it('can identify the details page', function () {
    pathTests.forEach( test => {
      urlUtils.isDetailPage(test.path).should.equal(test.isDetail, `Failed for test ${test.path} is ${test.isDetail}`)
    })
  })

/* granule url matching is part of #445
  it('can identify the granule list page', function () {
    pathTests.forEach( test => {
      urlUtils.isGranuleListPage(test.path).should.equal(test.isGranuleList, `Failed for test ${test.path} is ${test.isGranuleList}`)
    })
  })
*/
  it('can identify the collection id on the detail page', function () {
    pathTests.forEach( test => {
      const collectionId = urlUtils.getCollectionIdFromDetailPath(test.path)

      if(test.collectionIdFromDetailPath) {
        collectionId.should.equal(test.isDetail ? test.collectionIdFromDetailPath : null, `Failed for test ${test.path} expected collection id  ${test.collectionIdFromDetailPath}`)
      } else {
        expect(collectionId).to.be.null
      }
    })
  })

/* granule url matching is part of #445
  it('can identify the collection id on the granule list page', function () {
    pathTests.forEach( test => {
      const collectionId = urlUtils.getCollectionIdFromGranuleListPath(test.path)

      if(test.collectionIdFromGranuleListPath) {
        collectionId.should.equal(test.isGranuleList ? test.collectionIdFromGranuleListPath : null, `Failed for test ${test.path} expected collection id  ${test.collectionIdFromGranuleListPath}`)
      } else {
        expect(collectionId).to.be.null
      }
    })
  })
*/
})
