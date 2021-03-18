const processBodyData = require('./transformUtils');
const yargs = require('yargs');
const fs = require('fs');
const linksProcess = require('./sitemapIndex');
const axios = require('axios');
const https = require('https');
require('dotenv').config()


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
const webBase =  process.env.WEB_BASE; 
const pageSize = 10 //argv.pageSize


console.log("Env Search API: " + process.env.SEARCH_API_BASE);
console.log("Env Web: " + process.env.WEB_BASE);

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

//recursively pages API
let pageApi = async function (options, collectionList) {
  console.log("Requesting page for data after: " + options.data.search_after[0]);
  await axios(options)
    .then((response) => {
      console.log("Response status: " + response.status);
      if (response.status == 200) {
        let body = response.data;
        //If we got data back, process it and then keep going until we dont have anymore data
        // TODO catch body.error. Check if response status is not 200
        if (body && body.data.length > 0) {
          //grab the last staged date, we will need it for the subsequent request
          const lastStagedDate = body.data[body.data.length - 1].attributes.stagedDate;
          //update the options
          options.data.search_after = [lastStagedDate];
          //create the data structure we need for the sitemap tool
          var bodyDataObjectList = processBodyData(body);
          //add it to the list
          collectionList = [...collectionList, ...bodyDataObjectList];
          console.log("Received " + body.data.length + " items, continue paging...");
          //get the next page
          collectionList = pageApi(options, collectionList)
        } else {
          console.log("No more data. Generating sitemap...");
          options.data.search_after = [0];
        }
      }
    })
    //TODO - Future story more error handling 
    .catch(function (error) {
      console.log("ERROR");
      console.log(error);
    });
  return collectionList;
}

//page the api, create sitemap


let generateSitemap = function(){
    pageApi(options, []).then((listOfLinks) => linksProcess(listOfLinks));
}
module.exports = generateSitemap;