const webBase = "http://localhost/onestop"

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

//Helper method for convertCollectiontoXML's stagedDate to W3 DateTime format.
const Unix_TimeStamp = (t) =>{

  var dt = new Date(t)
  var n = dt.toISOString();

  return n;

}

module.exports = convertCollectionToXml;

/*
//Helper method for getCollectionPage's pagination
//cC needs to call getCollectionPage on the lastStagedDate of the previous collection
//It cannot infinitely loop and needs to continue until every granual of the collection is processed
const collectionCrawler = (lastStagedDate, loopCount, maxCollectionSize, pageSize, collectionApiUrl) => {
  console.log("CollectionCrawler");

  var totalProcessed = pageSize * loopCount;
  if(totalProcessed < maxCollectionSize){
     getCollectionPage(collectionApiUrl, pageSize, lastStagedDate);
      
  } 
}*/

//Helper method for processing body
const processBodyData = (body) => {

      body.data.forEach((d) => {
          console.log(convertCollectionToXml(webBase, d));
          console.log("Staged Date: " + d.attributes.stagedDate);
        })

};


//module.exports = collectionCrawler;
module.exports = processBodyData;