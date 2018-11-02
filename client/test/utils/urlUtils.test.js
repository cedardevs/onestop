import * as urlUtils from '../../src/utils/urlUtils'

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

    urls.forEach((url) => expect(urlUtils.isGovExternal(url)).toBeTruthy())
  })

  it('identify internal urls', function () {
    const urls = [
        '/search',
        '//www.ngdc.noaa.gov',
        'http://www.ngdc.noaa.gov',
        'https://www.ngdc.noaa.gov'
    ]

    urls.forEach((url) => expect(urlUtils.isGovExternal(url)).toBeFalsy())
  })

  it('can identify the details page', function () {
    pathTests.forEach( test => {
      expect(urlUtils.isDetailPage(test.path)).toBe(test.isDetail)
    })
  })

  it('can identify the granule list page', function () {
    pathTests.forEach( test => {
      expect(urlUtils.isGranuleListPage(test.path)).toBe(test.isGranuleList)
    })
  })

  it('can identify the collection id on the detail page', function () {
    pathTests.forEach( test => {
      const collectionId = urlUtils.getCollectionIdFromDetailPath(test.path)

      if(test.collectionIdFromDetailPath) {
        expect(collectionId).toBe(test.isDetail ? test.collectionIdFromDetailPath : null)
      } else {
        expect(collectionId).toBeNull()
      }
    })
  })

  it('can identify the collection id on the granule list page', function () {
    pathTests.forEach( test => {
      const collectionId = urlUtils.getCollectionIdFromGranuleListPath(test.path)

      if(test.collectionIdFromGranuleListPath) {
        expect(collectionId).toBe(test.isGranuleList ? test.collectionIdFromGranuleListPath : null)
      } else {
        expect(collectionId).toBeNull()
      }
    })
  })
})
