const processBodyData = require('./transformUtils').processBodyData
const yargs = require('yargs');
const linksProcess = require('./sitemapIndex')
const axios = require('axios');
const pageApi = require('./collectionRequest')


/*
const argv = yargs
  .option('api', {
    alias: 'a',
    description: 'Base URL for the catalog search api, e.g. http://localhost/onestop-search',
    type: 'string',
    required: true,
    default: 'localhost/onestop/api/search'
  })
  .option('website', {
    alias: 'w',
    description: 'Base URL for the catalog web page, e.g. http://localhost/onestop',
    type: 'string',
    required: true,
    default: 'localhost/onestop'
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
*/

//TODO - When pulling from cedar devs update API URL new path "cedardevs.org/onestop/api/search"
//TODO - Take entire collectionApiUrl from env file

const searchAPI = process.env.SEARCH_API_BASE;
const collectionApiUrl = new URL(`${searchAPI}`);
const pageSize = 10 //argv.pageSize

console.log("Generating sitemap for: " + process.env.SEARCH_API_BASE);

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

//Page the api, create sitemap
let generateSitemap = function(){
    pageApi(options, [], processBodyData).then((listOfLinks) => linksProcess(listOfLinks));
}
module.exports = generateSitemap;