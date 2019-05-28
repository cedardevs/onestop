import * as urlUtils from '../../src/utils/urlUtils'

describe('The URL Utils', function(){
  it('quick check it handles path change checks correctly', function(){
    expect(
      urlUtils.isPathNew(
        {pathname: 'abc', search: '?q=123'},
        {pathname: 'abc', search: '?q=123'}
      )
    ).toBeFalsy()
    expect(
      urlUtils.isPathNew(
        {pathname: 'abc', search: '?q=123'},
        {pathname: 'xyz', search: '?q=123'}
      )
    ).toBeTruthy()
    expect(
      urlUtils.isPathNew(
        {pathname: 'abc', search: '?q=123'},
        {pathname: 'abc', search: null}
      )
    ).toBeTruthy()
  })
  const pathTests = [
    {
      path: '/collections/details/foobar',
      isDetail: true,
      isGranuleList: false,
      collectionIdFromDetailPath: 'foobar',
    },
    {path: '/collections?q=foobar', isDetail: false, isGranuleList: false},
    {
      path: '/collections/granules/foobar?q=test',
      isDetail: false,
      isGranuleList: true,
      collectionIdFromGranuleListPath: 'foobar',
    },
  ]

  describe('identify external urls', function(){
    const urls = [
      '//www.google.com',
      'http://www.google.com',
      'https://www.google.com',
      'ftp://www.google.com',
      'ftps://www.google.com',
    ]

    urls.forEach(url => {
      it(`like ${url}`, function(){
        expect(urlUtils.isGovExternal(url)).toBeTruthy()
      })
    })
  })

  describe('identify internal urls', function(){
    const urls = [
      '/search',
      '//www.ngdc.noaa.gov',
      'http://www.ngdc.noaa.gov',
      'https://www.ngdc.noaa.gov',
    ]

    urls.forEach(url => {
      it(`like ${url}`, function(){
        expect(urlUtils.isGovExternal(url)).toBeFalsy()
      })
    })
  })

  describe('extractBaseFromKnownRoutes returns undefined if it does not match any route', function(){
    const routes = [ '/', '/onestop/' ]
    routes.forEach(test => {
      it(`knows ${test} does not explicitly match any routes`, function(){
        expect(urlUtils.extractBaseFromKnownRoutes(test)).toBeUndefined()
      })
    })
  })

  describe('extractBaseFromKnownRoutes can identify the base path correctly', function(){
    const routes = [
      {
        path: '/about',
        base: '/',
      },
      {
        path: '/onestop/help',
        base: '/onestop/',
      },
      {
        path: '/collections?q=foobar',
        base: '/',
      },
      {
        path: '/onestop/collections?q=foobar',
        base: '/onestop/',
      },
      {
        path: '/cedarsearch/collections',
        base: '/cedarsearch/',
      },
      {
        path: '/collections/granules/foobar',
        base: '/',
      },
      {
        path: '/onestop/collections/granules/foobar',
        base: '/onestop/',
      },
      {
        path: '/collections/details/foobar',
        base: '/',
      },
      {
        path: '/onestop/collections/details/foobar',
        base: '/onestop/',
      },
      {
        path: '/cedar-search/collections/details/foobar',
        base: '/cedar-search/',
      },
    ]
    routes.forEach(test => {
      it(`knows ${test.path} has base route ${test.base}`, function(){
        expect(urlUtils.extractBaseFromKnownRoutes(test.path)).toBe(test.base)
      })
    })
  })

  describe('can identify the page by path', function(){
    pathTests.forEach(test => {
      if (test.isDetail) {
        it(`knows ${test.path} is a detail page`, function(){
          expect(urlUtils.isDetailPage(test.path)).toBeTruthy()
        })
      }
      else {
        it(`knows ${test.path} is not a detail page`, function(){
          expect(urlUtils.isDetailPage(test.path)).toBeFalsy()
        })
      }

      if (test.isGranuleList) {
        it(`knows ${test.path} is a graule list`, function(){
          expect(urlUtils.isGranuleListPage(test.path)).toBeTruthy()
        })
      }
      else {
        it(`knows ${test.path} is not a granule list`, function(){
          expect(urlUtils.isGranuleListPage(test.path)).toBeFalsy()
        })
      }
    })
  })

  describe('can identify the collection id', function(){
    pathTests.forEach(test => {
      describe('from the detail path', function(){
        const collectionId = urlUtils.getCollectionIdFromDetailPath(test.path)

        if (test.collectionIdFromDetailPath && test.isDetail) {
          it(`knows ${test.path} has collectionId ${test.collectionIdFromDetailPath}`, function(){
            expect(collectionId).toBe(test.collectionIdFromDetailPath)
          })
        }
        else {
          it(`knows ${test.path} has no collectionId in detail path`, function(){
            expect(collectionId).toBeNull()
          })
        }
      })

      describe('from the granule list path', function(){
        const collectionId = urlUtils.getCollectionIdFromGranuleListPath(
          test.path
        )

        if (test.collectionIdFromGranuleListPath && test.isGranuleList) {
          it(`knows ${test.path} has collectionId ${test.collectionIdFromGranuleListPath}`, function(){
            expect(collectionId).toBe(test.collectionIdFromGranuleListPath)
          })
        }
        else {
          it(`knows ${test.path} has no collectionId in granule list path`, function(){
            expect(collectionId).toBeNull()
          })
        }
      })
    })
  })
})
