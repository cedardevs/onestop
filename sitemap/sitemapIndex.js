const { createReadStream, createWriteStream } = require('fs');
const { resolve } = require('path');
const { createGzip } = require('zlib')
const { Readable } = require('stream')
const {
  SitemapAndIndexStream,
  SitemapStream,
  lineSeparatedURLsToSitemapOptions,
  streamToPromise
} = require('sitemap');
const { example } = require('yargs');
const sitemapTotal = require('./generator');
const { get } = require('request');
const generator = require('./generator');

const exampleList = [{
  url: 'http://localhost/onestop/collections/details/0561ce74-bc07-4dd4-bf22-8c73befe9497',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.516Z'
},
{
  url: 'http://localhost/onestop/collections/details/15665e2a-dc84-482c-8e58-14d7d24b8f54',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.591Z'
},
{
  url: 'http://localhost/onestop/collections/details/dac3dbdf-fe19-4b86-aeb1-2fef83a26f51',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.652Z'
},
{
  url: 'http://localhost/onestop/collections/details/41633d5a-d8b3-482a-92ff-8ca6833c63d8',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.719Z'
},
{
  url: 'http://localhost/onestop/collections/details/150f59f9-7827-4eca-9156-6ae5c1640308',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.779Z'
},
{
  url: 'http://localhost/onestop/collections/details/62e7d363-0540-40ed-b89e-4a5e33a28ff2',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.848Z'
}];


const linkInfo = { url: '/page-1', changefreq: 'daily', priority: 0.3 };

const linksProcess = (links) => {

  console.log("Links: " + links);
  Readable.from(links).pipe(sms) // available as of node 10.17.0
}

module.exports.linksProcess = linksProcess;

const sms = new SitemapAndIndexStream({
  limit: 2, // defaults to 45k
  // SitemapAndIndexStream will call this user provided function every time
  // it needs to create a new sitemap file. You merely need to return a stream
  // for it to write the sitemap urls to and the expected url where that sitemap will be hosted
  hostname: 'https://cedardevs.org',
  getSitemapStream: (i) => {
    const sitemapStream = new SitemapStream({ hostname: 'https://cedardevs.org' });
    const path = `./sitemap-${i}.xml`;

    sitemapStream
      .pipe(createGzip()) // compress the output of the sitemap
      .pipe(createWriteStream(resolve(path + '.gz'))); // write it to sitemap-NUMBER.xml

    return [new URL(path, `https://cedardevs.org/subdir/${path}`).toString(), sitemapStream];
  },
  /*
  //Trim XML namespace at start of file
  xlmns: {
    news: true,
    xhtml: true,
    image: false,
    video: false,
  },
  */
});


 // Readable.from(generator.sitemapTotal).pipe(sms);
//getCollectionPage.forEach(item => sms.write(item))
  //sms.end();
// when reading from a file
// lineSeparatedURLsToSitemapOptions(
//   createReadStream('./testSitemapObject2.txt')
// )

// .pipe(sms)
// .pipe(createGzip())
// .pipe(createWriteStream(resolve('./sitemap-index.xml.gz')));



// or reading straight from an in-memory array
sms
.pipe(createGzip())
.pipe(createWriteStream(resolve('./sitemap-index.xml.gz')));


//sitemapTotal.forEach(item => sms.write(item + '\n'));
//sms.end(); // necessary to let it know you've got nothing else to write


//Debug testing for piping
console.log("sitemapTotal: " + sitemapTotal);

//console.log("getCollectionPage sitemapIndex: " + getCollectionPage);