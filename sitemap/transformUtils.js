const webBase = "http://localhost/onestop";

const convertCollectionToXml = (baseUrl, collection) => {

  var stagedDate = collection.attributes.stagedDate;
  var formattedDate = Unix_TimeStamp(stagedDate);

  return`
    <url>
        <loc>${baseUrl}/onestop/collections/details/${collection.id}</loc>
        <lastmod>${formattedDate}</lastmod>
        <changefreq>weekly</changefreq>
    </url>`
};

//Converted method of convertCollectionXML, we use this to create the object list
const convertCollectionToObject = (baseUrl, collection) => {

  var stagedDate = collection.attributes.stagedDate;
  var formattedDate = Unix_TimeStamp(stagedDate);
  var collObject = { url: `${baseUrl}/collections/details/${collection.id}`,
                    changefreq: `daily`, 
                    lastmod: `${formattedDate}`};


  // const object = { url: `/onestop/collections/details/${id}, changefreq: 'daily', lastmod: ${isoStagedDate}`};


  return collObject;
};

//Helper method for convertCollectiontoXML, transforms the collection's stagedDate to W3 DateTime format.
const Unix_TimeStamp = (t) =>{

  var dt = new Date(t)
  var n = dt.toISOString();

  return n;

}


//Takes in body & if not empty.null will
//TODO - Don't need switch statements as long as we do error checking before our default case
//TODO - Return a list in style of exampleList. Pipe that -> Readable(sitemapTotal)
//TODO - Refactor switch and get actual null error or empty body checks
const processBodyData = (body) => {
  var bodyDataObject = [];
  var i = 0;

  //Truthy check if(body), will evaluate to true if value is not null,undefined, NaN, empty string, 0, or false
  if ( body ) {
    body.data.forEach((d) => {
      //console.log(convertCollectionToObject(webBase, d));
      bodyDataObject[i++] = convertCollectionToObject(webBase, d);
      });
  } else {
    console.log("processBodyData body check error");
  }
  
    return bodyDataObject;
}

module.exports = processBodyData;
