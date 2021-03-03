const baseUrl = 'http://localhost/onestop';

//Collections used to mock payload & shared between unit tests
const collection1 = {
  attributes: {
    stagedDate: 1611269997516,
    fileIdentifier: 'gov.noaa.ngdc.mgg.photos:27',
    title: 'October 2005 Kashmir, Pakistan Images',
    serviceLinks: []
  },
  id: '0561ce74-bc07-4dd4-bf22-8c73befe9497',
  type: 'collection'
};
const collection2 = {
  attributes: {
    stagedDate: 1611269994444,
    fileIdentifier: 'gov.noaa.ngdc.mgg.photos:20',
    title: 'October 2005 Dolphin high fived me',
    serviceLinks: []
  },
  id: '0561ce74-bc07-4dd4-bf22-8c73bede1233',
  type: 'collection'
};
const collection3 = {
  attributes: {
    stagedDate: 2011269997949,
    fileIdentifier: 'gov.noaa.ngdc.mgg.photos:25',
    title: 'January 1990 Hurricane MLM Images',
  },
  id: '0561ce74-bc07-4dd4-ac44-8c73befe8488',
  type: 'collection'
};

//Null collection for error catching
const collectionNull = {
  attributes: {
    stagedDate: null,
    fileIdentifier: null,
    title: null,
  },
  id: null,
  type: null
};


//Payload mocking to mimic our actual response
const dataItems = [collection1, collection2, collection3];
const responseBody = {
  "data": dataItems
};

test('access collection1 inside responseBody', () => {


  expect(collection1).toBe(responseBody.data[0]);
});

test('access collection2 id inside responseBody', () => {

  expect(collection2.attributes.id).toBe(responseBody.data[1].attributes.id);
});

test('access collection3 type inside responseBody', () => {

  expect(collection3.attributes.type).toBe(responseBody.data[2].attributes.type);
});


test('generate sitemap xml to handle a response with 1 item', () => {
  const id = '0561ce74-bc07-4dd4-bf22-8c73befe9497';
  const isoStagedDate = "2021-01-21T22:59:57.516Z";
  const coll1Array = [collection1];
  const granual1Collection = {
    "data": coll1Array
  };
  /*
  const sitemap = `
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`
*/

  const collObject = {
    "url": `${baseUrl}/collections/details/${id}`,
    "changefreq": `daily`,
    "lastmod": `${isoStagedDate}`
  };

  const listForLibrary = [collObject];
  var expectedSitemap = processBodyData(granual1Collection);
  expect(expectedSitemap).toStrictEqual(listForLibrary);
});

test('generate sitemap objects for multiple collections', () => {
  //Mocked sitemap. processBodyData has two functions inside of it
  //processBodyData -> convertCollectionToXML -> Unix_TimeStamp
  //This test also confirms that convertCollectionToObject & Unix_TimeStamp work properly

  const collObject1 = {
    "url": `${baseUrl}/collections/details/${dataItems[0].id}`,
    "changefreq": `daily`,
    "lastmod": `2021-01-21T22:59:57.516Z`
  };

  const collObject2 = {
    "url": `${baseUrl}/collections/details/${dataItems[1].id}`,
    "changefreq": `daily`,
    "lastmod": `2021-01-21T22:59:54.444Z`
  };
  const collObject3 = {
    "url": `${baseUrl}/collections/details/${dataItems[2].id}`,
    "changefreq": `daily`,
    "lastmod": `2033-09-25T14:06:37.949Z`
  };

  const listForLibrary = [collObject1, collObject2, collObject3];

  var sitemapCompiled = processBodyData(responseBody);

  expect(sitemapCompiled).toStrictEqual(listForLibrary);
});


test('check for lastStagedDate in responseBody', () => {
  const lastDate = 2011269997949;

  //Last collection in responseBody is collection3
  let lastStagedDate = responseBody.data[dataItems.length - 1].attributes.stagedDate;
  expect(lastStagedDate).toBe(lastDate);
});


test('get the lastStagedDate after looping through dataItems', () => {
  const lastDate = 2011269997949;
  var lastStagedDate = 0;
  //var sitemapCompiled = ``;

  for (let i = 0; i < dataItems.length; i++) {
    //sitemapCompiled += convertCollectionToXml(baseUrl, dataItems[i]);
    lastStagedDate = dataItems[i].attributes.stagedDate
  }
  expect(lastStagedDate).toBe(lastDate);
});

const processBodyData = require('./transformUtils');


//TODO - Future tests