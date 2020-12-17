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
    .help()
    .alias('help', 'h')
    .argv;

const searchApiBase = argv.api
const collectionApiUrl = new URL(`${searchApiBase}/search/collection`)
const webBase = argv.website

const convertCollectionToXml = (baseUrl, collection) => {
    // TODO - format lastmod value as https://www.w3.org/TR/NOTE-datetime
    return`\
    <url>
        <loc>${baseUrl}/onestop/collections/details/${collection.id}</loc>
        <lastmod>${collection.attributes.stagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`
}

const getCollectionPage = (apiUrl, stagedDateAfter, beginDateAfter) => {
    console.log(`getting collections from ${collectionApiUrl}`)

    let options = {
        json: true,
        body: {
            "summary": false,
            "sort": [{"stagedDate": "asc"}, {"beginDate": "asc"}],
            "search_after": [stagedDateAfter, beginDateAfter],
            "queries": [],
            "page": {"max": 1000}
        }
    };
    request.post(apiUrl, options, (error, res, body) => {
        if (error) {
            return console.log(error)
        }

        if (!error && res.statusCode === 200) {
            console.log('<?xml version="1.0" encoding="UTF-8"?>')
            console.log('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">')
            body.data.forEach((d) => {
                console.log(convertCollectionToXml(webBase, d))
            })
            console.log('</urlset>')
        }
    });
}

getCollectionPage(collectionApiUrl, 0, 0)
// TODO - get stagedDate and beginDate from last item in page
// TODO - use previous stagedDate and beginDate values to retrive next page
// TODO - repeat until we have seen all pages
// TODO - handle need for multiple sitemaps with a <sitemapindex>





