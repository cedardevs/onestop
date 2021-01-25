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

//Helper method for convertCollectiontoXML's stagedDate to W3 DateTime format.
const Unix_TimeStamp = (t) =>{

  var dt = new Date(t)
  var n = dt.toISOString();

  return n;

}


//Processes granuals and converts to XML.
const processBodyData = (body, maxCollectionSize, choice) => {

  var bodyDataString = "";
  if(maxCollectionSize <= 0){
    choice = 'empty';
  }
  if(body == null || body == undefined || choice == undefined){
    choice = 'nullError';
  }

  /*'Choice' flag dictates what will execute
    Error checking with 'empty' & 'nullError' flags, 'Test' for transformUtils.test.js
  */
  switch (choice){
    case 'empty':
      console.log("Body is empty");
      break;

    case 'nullError':
     // console.log("Body is null or Error Thrown");
      break;

    case 'test':
      for(var i = 0; i < maxCollectionSize; i++){
        bodyDataString += convertCollectionToXml(webBase, body[i]);
      }
      return bodyDataString;


    default:
      //console.log("Default case");
        body.data.forEach((d) => {
          bodyDataString += convertCollectionToXml(webBase, d);
          })
         
  }

    return bodyDataString;
}

module.exports = processBodyData;
