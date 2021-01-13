const convertCollectionToXml = require('./transformUtils')

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
/*
test('generate sitemap xml for multiple collections', () => {
  const baseUrl = 'baseUrl';
  const id = 'abc-123';
  const stagedDate = 1533333269487;
  const lastStagedDate = 1610411428644;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";
  const collection = {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}};
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
  expect(convertCollectionToXml(baseUrl, collection)).toBe(sitemap);
});
*/
/*
test('Check for lastStagedDate in a collection', () => {
  const baseUrl = 'baseUrl';
  const id = 'abc-123';
  const stagedDate = 1533333269487;
  const lastDate = 1610411428644;
  const isoStagedDate = "2018-08-03T21:54:29.487Z";
  const collection = {"id":id, "type":null, "attributes":{"stagedDate":stagedDate}};
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
    </url>`
  expect(convertCollectionToXml(baseUrl, collection)).toBe(sitemap);
});*/
//Clean Clear tests
//TODO - Test for last staged date after recursion/looping
//TODO - Test for multiple collections (IE 10+)
//TODO - 