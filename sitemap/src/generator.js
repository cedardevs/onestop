const yargs = require('yargs')
const processBodyData = require('./transformUtils')
const linksProcess = require('./sitemapIndex')
const pageApi = require('./collectionRequest')


const argv = yargs
  .option('api', {
    alias: 'a',
    description: 'Base URL for the catalog search api, e.g. http://localhost/onestop-search',
    type: 'string',
    required: true
  })
  .option('website', {
    alias: 'w',
    description: 'Base URL for the catalog web page, e.g. http://localhost/onestop',
    type: 'string',
    required: true
  })
  .option('pageSize', {
    alias: 'n',
    description: 'Number of collections to request at a time',
    type: 'number',
    default: 10
  })
  .help()
  .alias('help', 'h')
  .argv;


//TODO - When pulling from cedar devs update API URL new path
//cedardevs.org/onestop/api/search
const searchApiBase = argv.api
const collectionApiUrl = new URL(`${searchApiBase}/search/collection`)
const webBase = argv.website
const pageSize = argv.pageSize

let options = {
  url: collectionApiUrl.toString(),
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
};

//page the api, create sitemap
pageApi(options, [], processBodyData).then((listOfLinks) => linksProcess(listOfLinks));