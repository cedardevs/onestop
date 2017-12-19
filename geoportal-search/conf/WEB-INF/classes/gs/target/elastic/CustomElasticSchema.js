
/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function(){

  gs.target.elastic.CustomElasticSchema = gs.Object.create(gs.target.elastic.ElasticSchema, {

    isVersion5Plus: {writable: true, value: true}, // TODO?

    geoField: {writable: true, value: "spatialBounding"},

    // TODO?
    sortables: {writable: true, value: {
      "title": "title",
      "modified": "modifiedDate"
    }},

    spatialInfo: {writable: true, value: {
      field: "spatialBounding",
      type: "geo_shape"
    }},

    timePeriodInfo: {writable: true, value: {
      field: "temporalBounding.beginDate",
      toField: "temporalBounding.endDate",
      nestedPath: "temporalBounding"
    }},

    buildAtomCategories: {value: function(task,item) {
      var categories = [], source = item["_source"];
      var itemType = task.val.chkStr(item["_type"]);
      var keywords = task.val.chkStrArray(source["keywords"]);
      var topicCategories = task.val.chkStrArray(source["topicCategories"]);
      if (itemType !== null && itemType.length > 0) {
        categories.push(gs.Object.create(gs.atom.Category).init({
          scheme: "type",
          term: itemType
        }));
      }
      if (Array.isArray(keywords)) {
        keywords.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            categories.push(gs.Object.create(gs.atom.Category).init({
              scheme: "keywords",
              term: v
            }));
          }
        });
      }
      if (Array.isArray(topicCategories)) {
        topicCategories.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            categories.push(gs.Object.create(gs.atom.Category).init({
              scheme: "topicCategory",
              term: v
            }));
          }
        });
      }
      return categories;
    }},

    buildAtomLinks: {value: function(task,item) {
      var links = [];
      var id = item["_id"];
      var source = item["_source"];
      var mimeType;
      var xmlUrl = null;  // TODO?
      var htmlUrl = null; // TODO?
      var jsonUrl = null; // TODO?
      var itemLinks = task.val.chkStrArray(source["links"]);

      if (typeof xmlUrl === "string" && xmlUrl.length > 0) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "alternate.xml",
          type: "application/xml",
          href: xmlUrl
        }));
      }
      if (typeof htmlUrl === "string" && htmlUrl.length > 0) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "alternate.html",
          type: "text/html",
          href: htmlUrl
        }));
      }
      if (typeof jsonUrl === "string" && jsonUrl.length > 0) {
        links.push(gs.Object.create(gs.atom.Link).init({
          rel: "alternate.json",
          type: "application/json",
          href: jsonUrl
        }));
      }
      if (Array.isArray(itemLinks)) {
        itemLinks.forEach(function(link){
          if (link && typeof link.linkUrl === "string" && link.linkUrl.length > 0) {
            mimeType = "unknown"; // TODO?
            links.push(gs.Object.create(gs.atom.Link).init({
              rel: "related",
              type: mimeType,
              href: link.linkUrl
            }));
          }
        });
      }

      return links;
    }},

    itemToAtomEntry: {value: function(task,item) {
      var source = item["_source"];
      var entry = gs.Object.create(gs.atom.Entry);
      entry.id = item["_id"];
      entry.title = task.val.chkStr(source["title"]);

      entry.published = task.val.chkStr(source["publicationDate"]); // TODO?
      entry.updated = task.val.chkStr(source["modifiedDate"]); // TODO?
      entry.category = this.buildAtomCategories(task,item);
      entry.link = this.buildAtomLinks(task,item);

      var summary = task.val.chkStr(source["description"]);
      if (summary !== null && summary.length > 0) {
        entry.summary = gs.Object.create(gs.atom.Text).init({
          type: "text",
          value: summary
        });
      }

      var author = task.val.chkStrArray(source["author"]); // TODO?
      if (Array.isArray(author)) {
        author.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            if (!entry.author) entry.author = [];
            entry.author.push(gs.Object.create(gs.atom.Person).init({
              tag: "author",
              name: v
            }));
          }
        });
      }

      var credits = task.val.chkStrArray(source["credits"]); // TODO?
      if (Array.isArray(credits)) {
        credits.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            if (!entry.contributor) entry.contributor = [];
            entry.contributor.push(gs.Object.create(gs.atom.Person).init({
              tag: "contributor",
              name: v
            }));
          }
        });
      }

      var rights = task.val.chkStrArray(source["rights"]); // TODO?
      if (Array.isArray(rights)) {
        rights.forEach(function(v){
          v = task.val.chkStr(v);
          if (v !== null && v.length > 0) {
            if (!entry.rights) entry.rights = [];
            entry.rights.push(gs.Object.create(gs.atom.Text).init({
              type: "text",
              value: v
            }));
          }
        });
      }

      if (this.geoField) {
        var geo = source[this.geoField];
        if(geo && geo.type) {
          if(geo.type === "polygon") {
            entry.bbox = gs.Object.create(gs.atom.BBox).init({
              xmin : geo.coordinates[0][0][0],
              ymin : geo.coordinates[0][2][1],
              xmax : geo.coordinates[0][2][0],
              ymax : geo.coordinates[0][0][1]
            });
          }
          else if (geo.type === "point") {
            entry.point = gs.Object.create(gs.atom.Point).init({
              x : geo.coordinates[0],
              y : geo.coordinates[1]
            });
          }
        }
      }

      return entry;
    }},

    itemToJson: {value: function(task,item) {
      return item;
    }}

  });

}());
