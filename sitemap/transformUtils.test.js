//Clean Clear tests

const granual1 =  { attributes: {
  stagedDate: 1611269997516,
  fileIdentifier: 'gov.noaa.ngdc.mgg.photos:27',
  title: 'October 2005 Kashmir, Pakistan Images',
  serviceLinks: []
},
id: '0561ce74-bc07-4dd4-bf22-8c73befe9497',
type: 'collection'
};
const granual2 =  { attributes: {
  stagedDate: 1611269994444,
  fileIdentifier: 'gov.noaa.ngdc.mgg.photos:20',
  title: 'October 2005 Dolphin high fived me',
  serviceLinks: []
},
id: '0561ce74-bc07-4dd4-bf22-8c73bede1233',
type: 'collection'
};
const granual3 =  { attributes: {
  stagedDate: 2011269997949,
  fileIdentifier: 'gov.noaa.ngdc.mgg.photos:25',
  title: 'January 1990 Hurricane MLM Images',
},
id: '0561ce74-bc07-4dd4-ac44-8c73befe8488',
type: 'collection'
};

var granCollection = [granual1, granual2, granual3];
const baseUrl = 'http://localhost/onestop';

test('granual1 payload mocking for collection.attributes', () => {
  const id = '0561ce74-bc07-4dd4-bf22-8c73befe9497';
  const stagedDate = 1611269997516;
  const sampleCollection = {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}};

  expect(granual1.attributes.stagedDate).toBe(sampleCollection.attributes.stagedDate);
});

test('generate sitemap xml for single collection', () => {
  const id = '0561ce74-bc07-4dd4-bf22-8c73befe9497';
  const stagedDate = 1611269997516;
  const collSize = 1;
  const isoStagedDate = "2021-01-21T22:59:57.516Z";
  const sitemap = `
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`

  var expectedSitemap = processBodyData(granCollection, collSize, 'test');
  expect(expectedSitemap).toBe(sitemap);
});

test('generate sitemap xml for multiple collections', () => {
  const stagedDate = 1533333269487;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";

  //TODO unique IDs
  //Make global variables to share between tests
  //Test to update options & test the response of calling 2nd iteration with new lastStagedDate
  const collection = [{"id":granual1.attributes.id, "type":granual1.attributes.type, "attributes":{"stagedDate":"2021-01-21T22:59:57.516Z"}},
                      {"id":granual2.attributes.id, "type":granual2.attributes.type, "attributes":{"stagedDate":"2021-01-21T22:59:54.444Z"}},
                      {"id":granual3.attributes.id, "type":granual3.attributes.type, "attributes":{"stagedDate":"2033-09-25T14:06:37.949Z"}}];
  const sitemap = `
    <url>
        <loc>${baseUrl}/onestop/collections/details/${granCollection[0].id}</loc>
        <lastmod>${collection[0].attributes.stagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>
    <url>
        <loc>${baseUrl}/onestop/collections/details/${granCollection[1].id}</loc>
        <lastmod>${collection[1].attributes.stagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>
    <url>
        <loc>${baseUrl}/onestop/collections/details/${granCollection[2].id}</loc>
        <lastmod>${collection[2].attributes.stagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`

    var sitemapCompiled = "";
    //for(let i = 0; i < collection.length; i++){
      sitemapCompiled = processBodyData(granCollection, collection.length, 'test');
      //console.log("for loop sitemap: " + processBodyData(granCollection[i], collection.length, 'test'));
     // console.log(convertCollectionToXml(baseUrl, collection[i]));
   // }
     var stringX = ""
  expect(sitemapCompiled).toBe(sitemap);
});


test('Check for lastStagedDate in a collection', () => {
  const id = 'abc-123';
  const stagedDate = 1533333269487;
  const lastDate = 1610411428644;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";
  const collection = [{"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":isoStagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":isoStagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":lastDate}}];

    //Testing Generator.js "let lastStagedDate = body.data[pageSize-1].attributes.stagedDate"
    let lastStagedDate = collection[collection.length-1].attributes.stagedDate;

  expect(lastStagedDate).toBe(lastDate);
});


test('Check for lastStagedDate after looping through a collection', () => {
  const baseUrl = 'baseUrl';
  const id = 'abc-123';
  const stagedDate = 1533333269487;
  const lastDate = 1610555555555;
  let lastStagedDate = 66666666;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";
  const collection = [{"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":isoStagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":lastDate}}];
  var sitemapCompiled = ``;
  const sitemap = `
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`


    for(let i = 0; i < collection.length; i++){

      sitemapCompiled += convertCollectionToXml(baseUrl, collection[i]);
      lastStagedDate = collection[i].attributes.stagedDate
    }
    //lastStagedDate = collection[collection.length-1].attributes.stagedDate;
  expect(lastStagedDate).toBe(lastDate);
});

const processBodyData = require('./transformUtils');
//TODO
/*
test('Pipe generated sitemap xml into a file', () => {

});
*/
const convertCollectionToXml = require('./transformUtils')
//const processBodyData = require('./transformUtils')
