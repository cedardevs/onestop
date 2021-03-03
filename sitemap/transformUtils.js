const webBase = "http://localhost/onestop";

const convertCollectionToXml = (baseUrl, collection) => {

  var stagedDate = collection.attributes.stagedDate;
  var formattedDate = Unix_TimeStamp(stagedDate);

  return `
    <url>
        <loc>${baseUrl}/onestop/collections/details/${collection.id}</loc>
        <lastmod>${formattedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`
};

//Create object list in the format sitemapIndex.js prefers
const convertCollectionToObject = (baseUrl, collection) => {

  var stagedDate = collection.attributes.stagedDate;
  var formattedDate = Unix_TimeStamp(stagedDate);
  var collObject = {
    url: `${baseUrl}/collections/details/${collection.id}`,
    changefreq: `daily`,
    lastmod: `${formattedDate}`
  };

  return collObject;
};

//Helper method for convertCollectiontoXML, transforms the collection's stagedDate to W3 DateTime format.
const Unix_TimeStamp = (t) => {

  var dt = new Date(t)
  var n = dt.toISOString();

  return n;

}


//Call order: Generator.js->processBodyData. processBodyData -> convertCollectionToObject -> Unix_Timestamp
//Returns an object formatted list of the body input
const processBodyData = (body) => {
  var bodyDataObject = [];
  var i = 0;

  //Truthy check if(body), will evaluate to true if value is not null,undefined, NaN, empty string, 0, or false
  if (body) {
    body.data.forEach((d) => {
      bodyDataObject[i++] = convertCollectionToObject(webBase, d);
    });
  } else {
    console.log("processBodyData body check error");
  }

  return bodyDataObject;
}

module.exports = processBodyData;