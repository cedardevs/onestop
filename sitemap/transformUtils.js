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
const processBodyData = (body, maxCollectionSize) => {
  let choice = "";

  var bodyDataObject = [];
  var i = 0;
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
          bodyDataObject[i++] = convertCollectionToObject(webBase, d);
          });
         
  }

    return bodyDataObject;
}

module.exports = processBodyData;
