const convertCollectionToXml = require('./transformUtils');
//const downloadString = require('./transformUtils');
const request = require('request');
const yargs = require('yargs');
const fs = require('fs');
const { SSL_OP_SSLEAY_080_CLIENT_DH_BUG } = require('constants');

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

const searchApiBase = argv.api
const collectionApiUrl = new URL(`${searchApiBase}/search/collection`)
const webBase = argv.website
const pageSize = argv.pageSize

const getCollectionPage = (apiUrl, size, stagedDateAfter) => {
    console.log(`getting collections from ${collectionApiUrl}`)

    let options = {
        json: true,
        body: {
            "summary": false,
            "sort": [{"stagedDate": "asc"}],
            "search_after": [stagedDateAfter],
            "queries": [],
            "page": {"max": size}
        }
    };
    request.post(apiUrl, options, (error, res, body) => {
        if (error) {
            return console.log(error)
        }

        if (!error && res.statusCode === 200) {
            console.log('<?xml version="1.0" encoding="UTF-8"?>')
            console.log('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">')
            /* Example sitemap index formula.
            <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                <sitemap>
                    <loc>http://www.data.noaa.gov.com/sitemap1.xml.gz</loc>
                    <lastmod>YYYY-MM-DDThh:mm:ssTZD</lastmod>
                </sitemap>
                <sitemap>
                    <loc>http://www.data.noaa.gov.com/sitemap2.xml.gz</loc>
                    <lastmod>YYYY-MM-DDThh:mm:ssTZD</lastmod>
                </sitemap>
            </sitemapindex>
            */
           //TODO - Error check for body.data. If not there body.error expected. If body.data is there check
           // For body.data length > 0
           //Throw error and exit
           let lastStagedDate = body.data[pageSize-1].attributes.stagedDate;
           let maxCollectionSize = body.meta.total;


           //Helper method for looping through each granual in collection, replaces below code
           
           
            //processBodyData(body, maxCollectionSize);
            var bodyDataString = ``;
  

            //console.log(body);
                if(maxCollectionSize > 0){
                body.data.forEach((d) => {
                  bodyDataString += convertCollectionToXml(webBase, d);
                   // console.log("Conv collec" + convertCollectionToXml(webBase, d));
                   // console.log("bodyDataString: " + bodyDataString);
                  })
                }
            console.log("\n ---- bodyDataString ---- \n" + bodyDataString + "\n ---- bodyDataString ---- \n");

            console.log('</urlset>')




         //Helper method crawlerCollection() to handle iterating/recursion through collection
         //Should call getCollectionPage or separate helper method to iterate through all collections

        console.log("\n-- DEBUG LOG --");
        console.log("LastStagedDate: " + lastStagedDate);
        console.log("maxCollectionSize: " + maxCollectionSize); //total granuals (body.meta.total)
        console.log("pageSize: " + pageSize);
        //console.log()



        
        }
    });
}



getCollectionPage(collectionApiUrl, pageSize, 0);

//TODO - Open/Close <urlset></urlset> at start end rather than each collection
// TODO - handle need for multiple sitemaps with a <sitemapindex>
// Sitemap index files may not have more than 50K Urls & no larger than 50MB

