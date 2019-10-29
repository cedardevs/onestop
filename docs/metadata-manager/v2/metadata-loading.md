# Data Load Process

## Uploading Metadata
The system stores ISO XML metadata records in order to power its search results.
All metadata documents must have a `<gmd:fileIdentifier>` tag containing either a
`<gco:CharacterString>` or a `<gmx:Anchor>` tag with the identifier. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi">
  ...
  <gmd:fileIdentifier>
    <gco:CharacterString>[IDENTIFIER]</gco:CharacterString>
  </gmd:fileIdentifier>
  ...
</gmi:MI_Metadata>  
```

Optionally, the record can also have a `<gmd:parentIdentifier>` tag (also containing
either a `<gco:CharacterString>` or a `<gmx:Anchor>` tag) to indicate that the record is
a child of another. In this case, the `parentIdentifier` of the child record must match
the `fileIdentifier` OR the `doi` of the parent record verbatim. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi">
  ...
  <gmd:fileIdentifier>
    <gco:CharacterString>[CHILD'S FILE IDENTIFIER]</gco:CharacterString>
  </gmd:fileIdentifier>
  <gmd:parentIdentifier>
    <gco:CharacterString>[PARENT'S FILE IDENTIFIER]</gco:CharacterString>
  </gmd:parentIdentifier>
  ...
</gmi:MI_Metadata>  
```

These documents can be uploaded, retrieved, and deleted from the system using REST-style
requests around the `/onestop-admin/metadata` resource endpoint.

HTTP Method | Endpoint                                  | Body      | Action
------------|-------------------------------------------|-----------|--------------------------
POST        | /onestop-admin/metadata                     | ISO XML   | Upload a metadata record <sup>[1](#postfootnote)</sup>
GET         | /onestop-admin/metadata/[internal-id]       | (none)    | Retrieve a metadata record <sup>[2](#idfootnote)</sup>
GET         | /onestop-admin/metadata?fileIdentifier={value};doi={value}   | (none)   | Retrieve a metadata record <sup>[3](#paramfootnote)</sup>
DELETE      | /onestop-admin/metadata/[internal-id]       | (none)    | Delete a metadata record <sup>[2](#idfootnote)</sup><sup>,</sup> <sup>[4](#delfootnote)</sup>
DELETE      | /onestop-admin/metadata?fileIdentifier={value};doi={value}      | (none)    | Delete a metadata record <sup>[3](#paramfootnote)</sup><sup>,</sup> <sup>[4](#delfootnote)</sup>

- <a href="postfootnote">1</a>: Note that POSTing an XML record with the same fileIdentifier or DOI as a previously-POSTed record will result in replacing that record.
- <a href="idfootnote">2</a>: The response to POSTing a record will include an internal ID which is used in these requests.
- <a href="paramfootnote">3</a>: Records can also be retrieved or deleted by providing the associated fileIdentifier and/or DOI value(s) as request parameters. If fileIdentifier and DOI provided match multiple records, *all found records will be returned or deleted*.
- <a href="delfootnote">4</a>: Note that DELETEing a collection-level metadata record will result in the deletion of all associated granules too UNLESS there is an included `recursive=false` in the query string (e.g, `DELETE {...}/onestop-admin/metadata/123?recursive=false`). Use of this endpoint for facilitating collection-level metadata updates should be limited to breaking changes that require a re-upload of granules -- such as a fileIdentifier change.
