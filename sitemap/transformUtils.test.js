//Clean Clear tests

test('generate sitemap xml for single collection', () => {
  const baseUrl = 'baseUrl';
  const id = 'abc-123';
  const stagedDate = 1533333269487;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";
  const collection = {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}};
  const sitemap = `
    <url>
        <loc>${baseUrl}/onestop/collections/details/${id}</loc>
        <lastmod>${isoStagedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`
  expect(convertCollectionToXml(baseUrl, collection)).toBe(sitemap);
});

test('generate sitemap xml for multiple collections', () => {
  const baseUrl = 'baseUrl';
  const id = 'abc-123';
  const stagedDate = 1533333269487;
  const lastStagedDate = 1610411428644;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";
  const collection = [{"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}},
                      {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}}];
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
      //sitemapCompiled.concat(convertCollectionToXml(baseUrl, collection[i]));
      //console.log(collection[i].attributes.stagedDate);
      //console.log(convertCollectionToXml(baseUrl, collection[i]));
      sitemapCompiled += convertCollectionToXml(baseUrl, collection[i]);
     // console.log(convertCollectionToXml(baseUrl, collection[i]));
    }
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

//TODO
/*
test('Pipe generated sitemap xml into a file', () => {
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
*/
const convertCollectionToXml = require('./transformUtils')
//const processBodyData = require('./transformUtils')
