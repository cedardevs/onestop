const axios = require('axios')

//recursively pages API
let pageApi = async function (options, collectionList, processFunction) {
  console.log("Requesting page for data after: " + options.data.search_after[0]);
  await axios(options)
    .then((response) => {
      console.log("Response status: " + response.status);
      if (response.status == 200) {
        let body = response.data;
        //If we got data back, process it and then keep going until we dont have anymore data
        // TODO catch body.error. Check if response status is not 200
        if (body && body.data.length > 0) {
          //grab the last staged date, we will need it for the subsequent request
          const lastStagedDate = body.data[body.data.length - 1].attributes.stagedDate;
          //update the options
          options.data.search_after = [lastStagedDate];
          //create the data structure we need for the sitemap tool
          var bodyDataObjectList = processFunction(body);
          //add it to the list
          collectionList = [...collectionList, ...bodyDataObjectList];
          console.log("Received " + body.data.length + " items, continue paging...");
          //get the next page
          collectionList = pageApi(options, collectionList, processFunction)
        } else {
          console.log("No more data. Generating sitemap...");
        }
      }
    })
    //TODO - Future story more error handling
    .catch(function (error) {
      console.log("ERROR");
      console.log(error);
    });
  return collectionList;
}

module.exports = pageApi
module.exports.default = pageApi