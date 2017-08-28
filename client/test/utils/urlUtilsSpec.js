import '../specHelper'
import * as urlUtils from '../../src/utils/urlUtils'

describe('The URL Utils', function () {
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
})
