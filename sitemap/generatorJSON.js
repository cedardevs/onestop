const yargs = require('yargs')
const axios = require('axios')
const fs = require('fs')
const path = require('path')

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

function processResponseData(response) {
  return Array.from(response.data.data).map((d) => {
    return { url: `${webBase}/collections/details/${d.id}` }
  })
}

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

async function pageApi (axiosOptions, collectionList) {
  console.log("Requesting page for data after: " + axiosOptions.data.search_after[0])
  await axios(axiosOptions)
    .then((response) => {
      console.log("Response status: " + response.status)
      if (response.status === 200) {
        let body = response.data
        // If we got data back, process it and then keep going until we dont have anymore data
        if (body && body.data.length > 0) {
          // grab the last staged date, we will need it for the subsequent request
          const lastStagedDate = body.data[body.data.length - 1].attributes.stagedDate
          // update the axiosOptions
          axiosOptions.data.search_after = [lastStagedDate]
          // create the data structure we need for the sitemap tool
          const bodyDataObjectList = processResponseData(response)
          // add it to the list
          collectionList = [...collectionList, ...bodyDataObjectList]
          console.log("Received " + body.data.length + " items, continue paging...")
          // get the next page
          collectionList = pageApi(axiosOptions, collectionList)
        } else {
          console.log("No more data. Generating sitemap...")
        }
      }
    })
    .catch(function (error) {
      console.log("ERROR")
      console.log(error)
    })
  return collectionList
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


function transformCollectionAttributes(collections) {
  return collections
}

function transformCollectionsToCatalog(collections) {
  const dataset = transformCollectionAttributes(collections)
  return {
    conformsTo: "https://project-open-data.cio.gov/v1.1/schema",
    describedBy: "https://project-open-data.cio.gov/v1.1/schema/catalog.json",
    "@context": "https://project-open-data.cio.gov/v1.1/schema/data.jsonld",
    "@type": "dcat:Catalog",
    dataset: dataset,
  }
}

pageApi(axiosOptions, []).then(
  (list) => {
    const catalog = transformCollectionsToCatalog(list)
    fs.writeFileSync(path.resolve(fileName), JSON.stringify(catalog, null, 2))
})