const processBodyData = require('./transformUtils');
const request = require('request');
const yargs = require('yargs');
const fs = require('fs');
const linksProcess = require('./sitemapIndex');

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

//Variable initialization to be ONLY shared between getCollectionPage & crawlerCollection 
{
var sitemapTotal = [];
var collCount = 0;
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
            //console.log("---Status Code flag---");
            if(body.meta.total == 0 || body == undefined || body == null){
                throw("error");
            }

            //let maxCollectionSize = body.meta.total;
            //collCount keeps track of our progress through the entire collection
            collCount += body.data.length;
            console.log("Collection Count processed: " + collCount);

            //Simple boolean checks for helper recursion & end of file
            if(keepGoing == true){
                //crawler returns 10 items
                crawlerCollection(body);
            }

            //Once finished processing the entire collection, print the sitemap
            if(keepGoing == false){ 

                //Flatten sitemapTotal to be one array
                sitemapTotal = sitemapTotal.flat();

                //Pipe sitemapTotal to sitemapIndex.js
                linksProcess(sitemapTotal);

                /*
                //---Useful debugging commands---
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

    // return sitemapTotal;

}
getCollectionPage(collectionApiUrl, pageSize, 0);



//Helper method to handle recursion for getCollectionPage
function crawlerCollection(body) {
    let lastStagedDate = body.data[body.data.length-1].attributes.stagedDate;

    //Store the processed paged body into bodyDataObject
    var bodyDataObject = processBodyData(body);


    //Recursion base case, checking for last page to end keepGoing
    //Body will be of size 'pageSize' unless on the last page
    if(body.data.length > 0 && body.data.length < pageSize){
        sitemapTotal.push(bodyDataObject);
        keepGoing = false;
     }
     //If collection is not empty and not on last page, recursively call gCP() with new lastStagedDate to update options
     else if(body.data.length > 0){
        sitemapTotal.push(bodyDataObject); 
        return getCollectionPage(collectionApiUrl, pageSize, lastStagedDate);
    }


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

