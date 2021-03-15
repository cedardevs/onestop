const yargs = require('yargs')
const fs = require('fs')
const path = require('path')
const pageApi = require('./collectionRequest')
const mime = require('mime-types')
const _ = require('lodash')
const Unix_TimeStamp = require('./transformUtils').Unix_TimeStamp

const argv = yargs
  .option('api', {
    alias: 'a',
    description: 'Base URL for the catalog search api, e.g. http://localhost/onestop-search',
    type: 'string',
    default: 'http://localhost/onestop/api/search'
  })
  .option('website', {
    alias: 'w',
    description: 'Base URL for the catalog web page, e.g. http://localhost/onestop',
    type: 'string',
    default: 'http://localhost/onestop'
  })
  .option('page-size', {
    alias: 'n',
    description: 'Number of collections to request at a time',
    type: 'number',
    default: 10
  })
  .option('output-file', {
    alias: 'o',
    description: 'Name of the output JSON file',
    type: 'string',
    default: 'data'
  })
  .help()
  .alias('help', 'h')
  .argv


const searchApiBase = argv.api
const webBase = argv.website
const collectionApiUrl = `${searchApiBase}/search/collection`
const pageSize = argv['page-size']
const fileName = argv['output-file'] + ".json"

let axiosOptions = {
  url: collectionApiUrl,
  method: 'POST',
  json: true,
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  },
  data: {
    "summary": false,
    "sort": [{
      "stagedDate": "asc"
    }],
    "search_after": [0],
    "queries": [],
    "page": {
      "max": pageSize
    }
  }
}

const collectionToOpenDataMapping = {
  title: "title",
  description: "description",
  keyword: "keywords",
  modified: (collection) => Unix_TimeStamp(collection.attributes.stagedDate),
  identifier: (collection) => {
    return collection.attributes.doi || collection.id
  },
  distribution: (collection) => {
    const links = collection.attributes.links
    return links.map((link) => {
      const mediaType = mime.lookup(new URL(link.linkUrl).pathname)
      const accessFields = (mediaType) ? {
        downloadURL: link.linkUrl,
        mediaType
      } : {
        accessURL: link.linkUrl
      }

      return {
        "@type": "dcat:Distribution",
        title: link.linkName,
        description: link.linkDescription,
        ...accessFields
      }
    })
  },
  landingPage: (collection) => `${webBase}/collections/details/${collection.id}`,
  // TODO: spatial mapping
  spatial: (collection) => {
    return "Worldwide"
  },
  temporal: (collection) => {
    const endDate = collection.attributes.endDate || new Date().toISOString()
    return `${collection.attributes.beginDate}/${endDate}`
  },
  bureauCode: () => ["006:048"], // NOAA Bureau Code
  programCode: () => ["006:059"], // NESDIS Program Code
  accessLevel: () => "public",
  language: () => ["en-US"],
  publisher: () => {
    return {
      '@type': 'org:Organization',
      'name': 'Department of Commerce',
    }
  },
  license: () => "http://www.usa.gov/publicdomain/label/1.0/", // or https://creativecommons.org/publicdomain/zero/1.0/ ?
  // TODO: contact information
  contactPoint: () => {
    return {
      '@type': 'vcard:Contact',
      'fn': 'Foo Bar',
      'hasEmail': 'mailto:foo@bar.gov',
    }
  }
}

function processResponseData(body) {
  return transformCollectionAttributes(Array.from(body.data))
}

/**
 * `collectionToOpenDataMapping` contains a mapping of Open Data schema attributes and maps those to either
 * a corresponding attribute name in `collection.attributes`, OR contains a function that takes in the collection object
 * in order to do more ad-hoc mappings
 */
function transformCollectionAttributes(collections) {
  return collections.map((collection) => {
    const mappedCollection = {}
    for (const [openDataAttr, onestopAttr] of _.entries(collectionToOpenDataMapping)) {
      if (typeof onestopAttr === "function") {
        mappedCollection[openDataAttr] = onestopAttr(collection)
      } else {
        mappedCollection[openDataAttr] = collection.attributes[onestopAttr]
      }
    }
    return mappedCollection
  })
}

function transformCollectionsToCatalog(collections) {
  return {
    conformsTo: "https://project-open-data.cio.gov/v1.1/schema",
    describedBy: "https://project-open-data.cio.gov/v1.1/schema/catalog.json",
    "@context": "https://project-open-data.cio.gov/v1.1/schema/data.jsonld",
    "@type": "dcat:Catalog",
    dataset: collections,
  }
}

pageApi(axiosOptions, [], processResponseData).then(
  (list) => {
    const catalog = transformCollectionsToCatalog(list)
    fs.writeFileSync(path.resolve(fileName), JSON.stringify(catalog, null, 2))
})