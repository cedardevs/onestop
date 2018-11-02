import * as urlUtils from '../../src/utils/urlUtils'

describe('The URL Utils', function () {
  const pathTests = [
    {path: 'collections/details/foobar', isDetail: true, isGranuleList: false, collectionIdFromDetailPath: 'foobar'},
    {path: 'collections?q=foobar', isDetail: false, isGranuleList: false},
    {path: 'collections/granules/foobar?q=test', isDetail: false, isGranuleList: true, collectionIdFromGranuleListPath: 'foobar'},
  ]

  describe('identify external urls', function () {
    const urls = [
        '//www.google.com',
        'http://www.google.com',
        'https://www.google.com',
        'ftp://www.google.com',
        'ftps://www.google.com'
    ]

    urls.forEach((url) => {
      it(`like ${url}`, function () {
        expect(urlUtils.isGovExternal(url)).toBeTruthy()
      })
    })
  })

  describe('identify internal urls', function () {
    const urls = [
        '/search',
        '//www.ngdc.noaa.gov',
        'http://www.ngdc.noaa.gov',
        'https://www.ngdc.noaa.gov'
    ]

    urls.forEach((url) => {
      it(`like ${url}`, function () {
        expect(urlUtils.isGovExternal(url)).toBeFalsy()
      })
    })
  })

  describe('can identify the page by path', function () {
    pathTests.forEach( test => {
      if(test.isDetail) {
        it(`knows ${test.path} is a detail page`, function () {
          expect(urlUtils.isDetailPage(test.path)).toBeTruthy()
        })
      } else {
        it(`knows ${test.path} is not a detail page`, function () {
          expect(urlUtils.isDetailPage(test.path)).toBeFalsy()
        })
      }

      if(test.isGranuleList) {
        it(`knows ${test.path} is a graule list`, function () {
          expect(urlUtils.isGranuleListPage(test.path)).toBeTruthy()
        })
      } else {
        it(`knows ${test.path} is not a granule list`, function () {
          expect(urlUtils.isGranuleListPage(test.path)).toBeFalsy()
        })
      }

    })
  })

  describe('can identify the collection id', function () {
    pathTests.forEach( test => {

      describe('from the detail path', function () {
        const collectionId = urlUtils.getCollectionIdFromDetailPath(test.path)

        if(test.collectionIdFromDetailPath && test.isDetail) {
          it(`knows ${test.path} has collectionId ${test.collectionIdFromDetailPath}`, function () {
            expect(collectionId).toBe(test.collectionIdFromDetailPath)
          })
        } else {
          it(`knows ${test.path} has no collectionId`, function () {
            expect(collectionId).toBeNull()
          })
        }
      })

      describe('from the granule list path', function () {
        const collectionId = urlUtils.getCollectionIdFromGranuleListPath(test.path)

        if(test.collectionIdFromGranuleListPath && test.isGranuleList) {
          it(`knows ${test.path} has collectionId ${test.collectionIdFromGranuleListPath}`, function () {
            expect(collectionId).toBe(test.collectionIdFromGranuleListPath)
          })
        } else {
          it(`knows ${test.path} has no collectionId`, function () {
            expect(collectionId).toBeNull()
          })
        }
      })

    })
  })
})
