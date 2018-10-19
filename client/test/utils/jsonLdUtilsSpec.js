import '../specHelper'
// import { toJsonLd, doiToJsonLd } from '../../src/utils/jsonLdUtils'
import * as util from '../../src/utils/jsonLdUtils'
import {assert} from 'chai'

describe('The jsonLdUtils', function() {

  it('creates a very simple JSON-LD object', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)"
    }

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)"
}`)
  })

  it('adds some DOI specific fields if the doi is provided', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      doi: "doi:10.1234/ABCDEFGH"
    }

    assert.equal(util.doiToJsonLd(input), `
  "alternateName": "doi:10.1234/ABCDEFGH",
  "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"`)

    assert.equal(util.doiToJsonLd({}), null, 'no doi in map should return null for doiToJsonLd helper')

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "alternateName": "doi:10.1234/ABCDEFGH",
  "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH"
}`)
  })

  it('adds image if thumbnail is provided', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      thumbnail: "http://example.com/thumbnail"
    }

    assert.equal(util.thumbnailToJsonLd(input), `
  "image": {
    "@type": "ImageObject",
    "url" : "http://example.com/thumbnail",
    "contentUrl" : "http://example.com/thumbnail"
  }`)

    assert.equal(util.thumbnailToJsonLd({}), null, 'no thumbnail in map should return null for thumbnailToJsonLd helper')

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "image": {
    "@type": "ImageObject",
    "url" : "http://example.com/thumbnail",
    "contentUrl" : "http://example.com/thumbnail"
  }
}`)
  })

  it('adds temporal if begin and end date provided', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      beginDate: "2018-10-19",
      endDate: "2019-01-02"
    }

    assert.equal(util.temporalToJsonLd(input), `
  "temporalCoverage": "2018-10-19/2019-01-02"`)

    assert.equal(util.temporalToJsonLd({}), null, 'no date in map should return null for temporalToJsonLd helper')

    assert.equal(util.temporalToJsonLd({beginDate: "2018-10-19"}), `
  "temporalCoverage": "2018-10-19/undefined"`, 'unbounded date range') // TODO this passed in the structured data parser, but is it really ok?

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "temporalCoverage": "2018-10-19/2019-01-02"
}`)
  })

  it('full item', function () {
    const input = {
      title: "the title of the record",
      description: "A rather long description (not!)",
      doi: "doi:10.1234/ABCDEFGH",
      thumbnail: "http://example.com/thumbnail",
      beginDate: "2018-10-19",
      endDate: "2019-01-02"
    }

    assert.equal(util.toJsonLd(input), `{
  "@context": "http://schema.org",
  "@type": "Dataset",
  "name": "the title of the record",
  "description": "A rather long description (not!)",
  "alternateName": "doi:10.1234/ABCDEFGH",
  "url": "https://accession.nodc.noaa.gov/doi:10.1234/ABCDEFGH",
  "sameAs": "https://data.nodc.noaa.gov/cgi-bin/iso?id=doi:10.1234/ABCDEFGH",
  "image": {
    "@type": "ImageObject",
    "url" : "http://example.com/thumbnail",
    "contentUrl" : "http://example.com/thumbnail"
  },
  "temporalCoverage": "2018-10-19/2019-01-02"
}`)
  })
})
