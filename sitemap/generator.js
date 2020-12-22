const request = require('request')
const yargs = require('yargs')
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

const convertCollectionToXml = (baseUrl, collection) => {
    // TODO - Store lastmod value into variable
    var stagedDate = collection.attributes.stagedDate;
    var formattedDate = Unix_TimeStamp(stagedDate);

    return`
    <url>
        <loc>${baseUrl}/onestop/collections/details/${collection.id}</loc>
        <lastmod>${formattedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`
}

const getCollectionPage = (apiUrl, size, stagedDateAfter, beginDateAfter) => {
    console.log(`getting collections from ${collectionApiUrl}`)

    let options = {
        json: true,
        body: {
            "summary": false,
            "sort": [{"stagedDate": "asc"}, {"beginDate": "asc"}],
            "search_after": [stagedDateAfter, beginDateAfter],
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

            body.data.forEach((d) => {
                console.log(convertCollectionToXml(webBase, d))
            })
            console.log('</urlset>')
        }
    });
}

//Helper method for convertCollectiontoXML's stagedDate to W3 DateTime format.
const Unix_TimeStamp = (t) =>{
     
    var dt = new Date(t)
     var n = dt.toISOString();

     return n;
    
 }

getCollectionPage(collectionApiUrl, pageSize, 0, 0)
// TODO - get stagedDate and beginDate from last item in page
// TODO - use previous stagedDate and beginDate values to retrive next page
// TODO - repeat until we have seen all pages
// TODO - handle need for multiple sitemaps with a <sitemapindex>
// Sitemap index files may not have more than 50K Urls & no larger than 50MB


/* Psuedocode for Crawler, very rough draft
 const collectionCrawler = (stagedDate, beginDate) =>{

    var lastStaged;
    var lastBegin;
    while(collection){
        if(sitemapindex.size>50000 or data full){
        create new sitemap index
        assign it to be new base index
    }
    convertCollectiontoXML into current sitemap
    

    if(last item on last page){
        lastStaged = current.stagedDate;
        lastBegin = current.beginDate;
        Either recursively call collectionCrawler(lastStaged, lastBegin);
        Or change staged/begin so when collection start again we reassign to new pages
    }

}                                                 
    
  */  
 