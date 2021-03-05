const yargs = require('yargs')
const fs = require('fs')
const path = require('path')
const pageApi = require('./collectionRequest')

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
  identifier: "id",
  distribution: "links",
  landingPage: (collection) => `${webBase}/collections/details/${collection.id}`,
  /*modified: "2013-10-01",
  publisher: {
    "@type": "org:Organization",
    "name": "Department of Defense"
  },
  contactPoint: {"@type": "vcard:Contact", "fn": "Aaron Graves", "hasEmail": "mailto:aaron.graves@whs.mil"},
  accessLevel: "public",
  license: "http://www.usa.gov/publicdomain/label/1.0/",
  spatial: "Worldwide",
  temporal: "2000-01-01/2000-12-31",
  language: ["en-US"],
  bureauCode: ["007:05"],
  programCode: ["007:053"]*/
}

function processResponseData(body) {
  return transformCollectionAttributes(Array.from(body.data))
}

function transformCollectionAttributes(collections) {
  // TODO: map the keys we get from onestop to the open data schema
  return collections.map((col) => {
    const { attributes, id } = col
    return { id, ...attributes }
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