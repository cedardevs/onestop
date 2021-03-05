const { createReadStream, createWriteStream} = require('fs');
const { resolve } = require('path');
const { createGzip } = require('zlib')
const { Readable } = require('stream')
const {
  SitemapAndIndexStream,
  SitemapStream,
  lineSeparatedURLsToSitemapOptions,
  streamToPromise
} = require('sitemap');

const linksProcess = (links) => {
  //console.log("Links 2: " + links);
  Readable.from(links).pipe(sms) // available as of node 10.17.0
}

module.exports = linksProcess;

const sms = new SitemapAndIndexStream({
  limit: 50, // defaults to 45k
  // SitemapAndIndexStream will call this user provided function every time
  // it needs to create a new sitemap file. You merely need to return a stream
  // for it to write the sitemap urls to and the expected url where that sitemap will be hosted
  hostname: 'https://cedardevs.org',
  getSitemapStream: (i) => {
    const sitemapStream = new SitemapStream({
      hostname: 'https://cedardevs.org'
    });
    const path = `./sitemap-${i}.xml`;

    sitemapStream
      .pipe(createGzip()) // compress the output of the sitemap
      .pipe(createWriteStream(resolve(path + '.gz'))); // write it to sitemap-NUMBER.xml

    return [new URL(path, `https://cedardevs.org/subdir/${path}`).toString(), sitemapStream];
  },
});

// or reading straight from an in-memory array
sms
  .pipe(createGzip())
  .pipe(createWriteStream(resolve('./sitemap-index.xml.gz')));

/*  Format passed into the sitemap library to generate sitemap files
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
  url: 'http://localhost/onestop/collections/details/62e7d363-0540-40ed-b89e-4a5e33a28ff2',
  changefreq: 'daily',
  lastmod: '2021-01-21T22:59:57.848Z'
}];
*/