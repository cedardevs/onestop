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

//Helper method for convertCollectiontoXML, transforms the collection's stagedDate to W3 DateTime format.
const Unix_TimeStamp = (t) =>{

  var dt = new Date(t)
  var n = dt.toISOString();

  return n;

}


//Takes in body & if not empty.null will
//TODO - Don't need switch statements as long as we do error checking before our default case
const processBodyData = (body, maxCollectionSize) => {
  let choice = "";
  var bodyDataString = "";
  if(maxCollectionSize <= 0){
    choice = 'empty';
  }
  if(body == null || body == undefined || choice == undefined){
    choice = 'nullError';
  }

  /*
    'Choice' flag dictates what will execute
    Error checking with 'empty' & 'nullError' flags, otherwise 'default'
  */
  switch (choice){
    case 'empty':
      console.log("Body is empty");
      break;

    case 'nullError':
      //console.log("Body is null or Error Thrown");
      break;


    default:
        body.data.forEach((d) => {
          bodyDataString += convertCollectionToXml(webBase, d);
          });
         
  }

    return bodyDataString;
}

module.exports = processBodyData;
