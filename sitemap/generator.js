const processBodyData = require('./transformUtils');
const yargs = require('yargs');
const fs = require('fs');
const linksProcess = require('./sitemapIndex');
const axios = require('axios');
const axiosRetry = require('axios-retry');
const https = require('https');


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



const getCollectionPage = (apiUrl, size, stagedDateAfter) => {

    var sitemapTotal = [];
    var collCount = 0, maxCollectionSize = 0, counter = 0;
    var lastStagedDate;

    console.log(`getting collections from ${collectionApiUrl}`)
    const stringURL = apiUrl.toString();
    //TODO - Double check if I need stringURL or can just pass apiURL once sitemapxml works
    let options = {
        url: stringURL,
        method: 'POST',
        json: true,
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'        
        },
        data: {
            "summary": false,
            "sort": [{"stagedDate": "asc"}],
            "search_after": [stagedDateAfter],
            "queries": [],
            "page": {"max": size}
        }
    };

   function newPage(updateOptions){
        axios(updateOptions)
        .then((response)=>{
            if(response.status == 200){
                console.log('\n' + "--Status 200--");

                let body = response.data;

                if((body) && body.data.length > 0){
                lastStagedDate = body.data[body.data.length-1].attributes.stagedDate;
                maxCollectionSize = body.meta.total;
                }
            
                //collCount keeps track of our progress through the entire collection
                console.log("lastStageDate: " + lastStagedDate);
                console.log("Max Collection Size: " + maxCollectionSize);
                console.log("collCount: " + collCount);

                //Calculate how many times we need to iterate
                var totalPages = Math.ceil(maxCollectionSize/pageSize);
                collCount += body.data.length;


                //TODO - Body.data.list == empty -> done
                if(counter > totalPages || body.data.length == 0){ //Already done, exit
                    console.log('\n' + "---Sitemap XML files generated---");
                    return;
                }
                else if(counter == totalPages){ //End case
                    //Flatten sitemapTotal to be one array
                    sitemapTotal = sitemapTotal.flat();
                    
                    //Pipe sitemapTotal to sitemapIndex.js
                    linksProcess(sitemapTotal);
                    counter++;
                    
                } else { //Recursion

                    var bodyDataObject = processBodyData(body);
                    sitemapTotal.push(bodyDataObject); 
                    
                
                    //console.log("search_before: " + options.data.search_after);
                    options.data.search_after = [lastStagedDate];
                    //console.log("search_after: " + options.data.search_after);                    
                    counter++;
                    console.log("Counter: " + counter);
                    newPage(options);
                    


                }

                return sitemapTotal;
            }
        }).catch(function (error) {
            if (error.response) {
              // Request made and server responded
              console.log(error.response.data);
              console.log(error.response.status);
              console.log(error.response.headers);
            } else if (error.request) {
              // The request was made but no response was received
              console.log(error.request);
            } else {
              // Something happened in setting up the request that triggered an Error
              console.log('Error', error.message);
            }
        
          });
          //return sitemapTotal;
    }


    newPage(options);

 

}






getCollectionPage(collectionApiUrl, pageSize, 0);


async function recursiveCrawl(body, sitemapTotal, options, counter, newPage){
                    var bodyDataObject = processBodyData(body);
                    sitemapTotal.push(bodyDataObject); 
                    options.data.search_after = lastStagedDate
                    counter++;
    return newPage();
}

//Helper method to handle recursion for getCollectionPage
//Previous iteration used crawlerCollection, now put all logic inside gCP();
function crawlerCollection(body) {
    let lastStagedDate = body.data[body.data.length-1].attributes.stagedDate;

    //Store the processed paged body into bodyDataObject
    var bodyDataObject = processBodyData(body);
    sitemapTotal.push(bodyDataObject); 


    //Recursion base case, checking for last page to end keepGoing
    //Body will be of size 'pageSize' unless on the last page
    console.log(body.data.length);
    if(body.data.length > 0 && body.data.length < pageSize){
        console.log("--crawlerCollecton 1st IF FLAG PASS---");
        keepGoing = false;
     }
     //If collection is not empty and not on last page, recursively call gCP() with new lastStagedDate to update options
     else if(body.data.length > 0){
        return getCollectionPage(collectionApiUrl, pageSize, lastStagedDate);
    }


}



    // OLD WORKING CODE - Deprecated Fetch library
    // request.post(apiUrl, options, (error, res, body) => {
    //     if (error) {
    //         return console.log(error)
    //     }

    //     if (!error && res.statusCode === 200 && keepGoing == true) {
    //         if(body.meta.total == 0 || body == undefined || body == null){
    //             throw("error");
    //         }

    //         //let maxCollectionSize = body.meta.total;
    //         //collCount keeps track of our progress through the entire collection
    //         collCount += body.data.length;
    //         console.log("Collection Count processed: " + collCount);

    //         //Simple boolean checks for helper recursion & end of file
    //         if(keepGoing == true){
    //             crawlerCollection(body);
    //         }

    //         //Once finished processing the entire collection, print the sitemap
    //         if(keepGoing == false){ 

    //             //Flatten sitemapTotal to be one array
    //             sitemapTotal = sitemapTotal.flat();

    //             //Pipe sitemapTotal to sitemapIndex.js
    //             linksProcess(sitemapTotal);

    //             /*
    //             //---Useful debugging commands---
    //             let lastStagedDate = body.data[body.data.length-1].attributes.stagedDate;
    //             console.log("\n-- DEBUG LOG --\n");
    //             console.log("LastStagedDate: " + lastStagedDate);
    //             console.log("maxCollectionSize: " + maxCollectionSize);
    //             console.log("pageSize: " + pageSize);
    //             console.log("body.length: " + body.data.length);
    //             */
    //             // Debug command to print out the page's collection
    //             //  body.data.forEach((d) => {
    //             //  console.log(d);
    //             //  })
                
    //         }
    //     }
        
    // });

    // return sitemapTotal;

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

