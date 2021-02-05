const processBodyData = require('./transformUtils');
const request = require('request');
const yargs = require('yargs');
const fs = require('fs');



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
var sitemapTotal = [];
var collCount = 0;
var i = 0;
var keepGoing = true;

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

        if (!error && res.statusCode === 200 && keepGoing == true) {
            //Error checking for body
            if(body.meta.total == 0 || body == undefined || body == null){
                throw("error");
            }
           
            let maxCollectionSize = body.meta.total;

            //collCount keeps track of our progress through the entire collection
            collCount += body.data.length;
            console.log("collCount: " + collCount);

            //Simple boolean checks for helper recursion & end of file
            if(keepGoing == true){
                crawlerCollection(body, maxCollectionSize);
            }

            //Once finished processing the entire collection, print the sitemap
            //TODO - Write to a file, Memory constraints, must use gzip
            if(keepGoing == false){ 
                console.log("\n-----SITEMAP FILE-----");
                console.log('<?xml version="1.0" encoding="UTF-8"?>')
                console.log('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">')
                console.log(sitemapTotal);
                console.log('</urlset>');
                console.log("-----SITEMAP FILE-----\n");


                /*
                //Useful debugging commands
                let lastStagedDate = body.data[body.data.length-1].attributes.stagedDate;
                console.log("\n-- DEBUG LOG --\n");
                console.log("LastStagedDate: " + lastStagedDate);
                console.log("maxCollectionSize: " + maxCollectionSize);
                console.log("pageSize: " + pageSize);
                console.log("body.length: " + body.data.length);
                */
                // Debug command to print out the page's collection
                //  body.data.forEach((d) => {
                //  console.log(d);
                //  })
                
            }
        }
    });
}



getCollectionPage(collectionApiUrl, pageSize, 0);

//Helper method to handle recursion for getCollectionPage
function crawlerCollection(body, maxCollectionSize) {

    let lastStagedDate = body.data[body.data.length-1].attributes.stagedDate;

    //bodyDataString processes each item in our body and converts it to XML
    var bodyDataString = processBodyData(body, maxCollectionSize);
    sitemapTotal[i++] = bodyDataString;


    //Recursion base case, checking for last page
    //Body will be of size 'pageSize' unless on the last page
    if(body.data.length > 0 && body.data.length < pageSize){
        sitemapTotal[i] = processBodyData(body, maxCollectionSize);
        keepGoing = false;
     } 

     //If collection is not empty and not on last page, recursively call gCP() with new lastStagedDate to update options
     else if(body.data.length > 0){
         return getCollectionPage(collectionApiUrl, pageSize, lastStagedDate);
     }

}



    /*
        1. Post Request & receive body
            - Know what each request looks like at each stage, inputs vs outputs
            - Send request, get response back, update options, resend request, update options, etc until end of data
        2. Process Body & loop (Choose Loop vs Recursion)
        3. Take lastStagedDate
        4. Call getCollectionPage with new lastStagedDate (pagination/search after)
        5. Method to update query option "search_after"
        6. Stop when Data is empty & Close XML
            -Sitemap Index Logic
            -File compression
        7. Write output to XML file & JSON file
    */

// TODO - handle need for multiple sitemaps with a <sitemapindex>
// Sitemap index files may not have more than 50K Urls & no larger than 50MB

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