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
/*
const sitemap = new SitemapStream({ hostname: 'http://cedardevs.org'});
sitemap.write({ url: '/page-1', changefreq: 'daily, priority: 0.3\n'});
sitemap.write('/page-2');
sitemap.end();


streamToPromise(sitemap)
  .then((sm) => console.log(sm.toString()))
  .catch(console.error);
*/


const linkInfo = { url: '/page-1', changefreq: 'daily', priority: 0.3 };

const sms = new SitemapAndIndexStream({
  limit: 150, // defaults to 45k
  // SitemapAndIndexStream will call this user provided function every time
  // it needs to create a new sitemap file. You merely need to return a stream
  // for it to write the sitemap urls to and the expected url where that sitemap will be hosted
  getSitemapStream: (i) => {
    const sitemapStream = new SitemapStream({ hostname: 'https://cedardevs.org' });
    const path = `./sitemap-${i}.xml`;

    sitemapStream
      .pipe(createGzip()) // compress the output of the sitemap
      .pipe(createWriteStream(resolve(path + '.gz'))); // write it to sitemap-NUMBER.xml

    return [new URL(path, `https://cedardevs.org/subdir/${path}`).toString(), sitemapStream];
  },
});

// when reading from a file
lineSeparatedURLsToSitemapOptions(
  createReadStream('./testSitemapObjectData.txt')
)
.pipe(sms)
.pipe(createGzip())
.pipe(createWriteStream(resolve('./sitemap-index.xml.gz')));

/*
// or reading straight from an in-memory array
sms
.pipe(createGzip())
.pipe(createWriteStream(resolve('./sitemap-index.xml.gz')));

*/
/*
const arrayOfSitemapItems = [linkInfo];
//Readable.from(arrayOfSitemapItems).pipe(sms) // available as of node 10.17.0
// or
arrayOfSitemapItems.forEach(item => sms.write(item + '\n'));
sms.end(); // necessary to let it know you've got nothing else to write
*/
