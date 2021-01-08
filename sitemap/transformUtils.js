const convertCollectionToXml = (baseUrl, collection) => {
  // TODO - Store lastmod value into variable
  var stagedDate = collection.attributes.stagedDate;
  var formattedDate = Unix_TimeStamp(stagedDate);
  console.log(formattedDate);

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