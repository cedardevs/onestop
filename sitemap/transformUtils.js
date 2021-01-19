const webBase = "http://localhost/onestop";

const convertCollectionToXml = (baseUrl, collection) => {
//module.exports.convertCollectionToXml = (baseUrl, collection) => {
//function convertCollectionToXml(baseUrl, collection){
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
/*
function downloadString(text, fileType, fileName) {
    var blob = new Blob([text], { type: fileType });

    var a = document.createElement('a');
    a.download = fileName;
    a.href = URL.createObjectURL(blob);
    a.dataset.downloadurl = [fileType, a.download, a.href].join(':');
    a.style.display = "none";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(function() { URL.revokeObjectURL(a.href); }, 1500);
    }
module.exports = downloadString;
*/

module.exports =  convertCollectionToXml;


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
/*
const processBodyData = (body, maxCollectionSize) => {
//module.exports.processBodyData = (body, maxCollectionSize) => {
//function processBodyData(body, maxCollectionSize){
  var bodyDataString = ``;
  

  //console.log(body);
      if(maxCollectionSize > 0){
      body.data.forEach((d) => {
        bodyDataString += convertCollectionToXml(webBase, d);
          console.log("Conv collec" + convertCollectionToXml(webBase, d));
          console.log("bodyDataString: " + bodyDataString);
        })
      }
   
};
*/
/*
module.exports = {
  convertCollectionToXml,
  processBodyData
}*/
//module.exports = processBodyData;


//TODO
//module.exports = collectionCrawler;
