<div align="center"><a href="/onestop/api">OneStop API</a></div>
<hr>

**Estimated Reading Time: 20 minutes**

# Search Query Syntax

## Table of Contents
* [Common Use Cases](#common-use-cases)
* [Supported Search Query Features](#supported-search-query-features)
    * [Default Query Behavior](#default-query-behavior)
    * [Terms, Phrases, And Groups](#terms-phrases-and-groups)
    * [Boolean Operators](#boolean-operators)
    * [Fuzziness Matching](#fuzziness-matching)
    * [Proximity Searches](#proximity-searches)
    * [Boosting Search Clause Relevance](#boosting-search-clause-relevance)
    * [Search By Field Name](#search-by-field-name)
        * [Search Fields Requiring An Exact Match With An Inexact Query](#search-fields-requiring-an-exact-match-with-an-inexact-query)
    * [Reserved characters](#reserved-characters)
    * [Wildcards](#wildcards)
    * [Regular Expressions](#regular-expressions)
* [Requesting Help](#requesting-help)

OneStop's underlying query is an Elasticsearch [query string query](https://www.elastic.co/guide/en/elasticsearch/reference/master/query-dsl-query-string-query.html#query-string-syntax). Much of the Elasticsearch guide information is relevant to OneStop, but below you will find information on default behavior, explanations of the advanced syntax features in relation to the OneStop-specific indices, and exactly what is and is not supported.  All of the following search features can be used in combination with each other to adjust the match precision of your results. Some slack can be introduced by modifying the proximity and fuzziness match levels, or narrow down the specifics with boolean operators and explicit fields.

## Common Use Cases
- [Match An Exact Phrase](#boolean-operators)
- [Search Against A Specific Field](#search-by-field-name)
- [Search Fields Requiring An Exact Match With An Inexact Query](#search-fields-requiring-an-exact-match-with-an-inexact-query)
- Specify What Should And Should Not Appear In My Results - [Boolean Operators](#boolean-operators), [fuzziness](#fuzziness-matching), and [proximity](#proximity-searches)

## Supported Search Query Features
- [Default Query Behavior](#default-query-behavior)
- [Terms, Phrases, And Groups](#terms-phrases-and-groups)
- [Boolean Operators](#boolean-operators)
- [Fuzziness Matching](#fuzziness-matching)
- [Proximity Searches](#proximity-searches)
- [Boosting Search Clause Relevance](#boosting-search-clause-relevance)
- [Search By Field Name](#search-by-field-name)
- [Reserved characters](#reserved-characters)
- [Wildcards](#wildcards)
- [Regular Expressions](#regular-expressions)

### Default Query Behavior
The primary goal of the OneStop query is to enable the user to quickly discover relevant results with nothing more than a simple term or phrase as input. Given that broad goal, the default behavior of the query is to look for *case-insensitive* matches against the most generically informative and universally populated fields of the metadata: primarily `title`, `description`, and `keywords`. These fields have different weights that will affect the overall relevance score of a match with your query text: matches in the `title` hold more weight than those in `keywords`, which holds more weight than those in `description`. This is done based on the assumption that the more succinct a field is, the more critical every word in that field becomes. However, based on user feedback and evolution in metadata parsing, the default fields searched against will always contain more than just these three fields. 

The current set of default fields, with their corresponding weights, can be found at the bottom of the [Search API's YAML configuration file](https://github.com/cedardevs/onestop/blob/master/search/src/main/resources/application.yml#L38). Please note that a running version of OneStop may not correlate to the latest development version though, so ensure you're looking at the correct tag branch for the version of OneStop you're using. The running version is located at the very bottom of the OneStop site footer. A version ending in "-SNAPSHOT" most often correlates to the `master` branch (where the above link resolves).

![Image showing how to change the branch tag on github](/onestop/images/api/gh-tag-dropdown.png)

The default behavior of the query also includes some more technical details that are described in detail in the next four sections.

### Terms, Phrases, And Groups
The text in your query can be thought of as a set of terms, phrases, and/or groupings of terms and/or phrases. By default, when you submit a words-only query (without any special characters), every word is treated as an optional term that may or may not appear in a returned document. A search for `sea surface temperature` thus would be a search for `sea` or `surface` or `temperature` (see the next section on [boolean operators](#boolean-operators) for the exact explanation, however, as a minimum match threshold is actually in place).

To group a set of terms, or words, into a phrase that should be treated as a single unit enclose the words in double quotes. A search for `"sea surface temperature"` will therefore only match records that have that exact phrase (see the [proximity searches](#proximity-searches) section for more details on how to adjust this). 

Finally, groups are denoted by enclosing a component of your query text with parentheses. Grouping is often required for clarity when using many of the following operators, and you will see groups in most examples.

### Boolean Operators
Using boolean operators in your query text allows you to be absolutely precise about the terms and phrases you do or do not want your results to match. Boolean operators effectively translate portions of your query text into clauses that `MUST` be present in results, clauses that `MUST NOT` be present, and clauses that `SHOULD` be present. Without any operators, a term or phrase is considered a `SHOULD` clause. For OneStop specifically, there is a minimum threshold of how many `SHOULD` terms/phrases in your query must match in order for a result to be returned. We have set this fairly high at 75%. If your query has 3 or less terms/phrases sans any operators, they all must match (otherwise the threshold is not met: 2/3 => 66%; 1/2 => 50%). After 4 terms/phrases, however, only 75% of the `SHOULD` clauses have to match to be considered a match. 

If you would like to ensure that a term or phrase `MUST` or `MUST NOT` appear in all results, the absolute simplest way to achieve this is by using `+` and `-` operators for `MUST` and `MUST NOT`, respectively.

Example query using preferred operators:

`temperature pressure +air -sea`

Alternatively, one can also use `AND`, `OR`, and `NOT` (alternatively `&&`, `||`, and `!`) to write a query with boolean operators. However, these boolean operators introduce operator precedence and require a far more complicated query structure.

The same logical query as above using the alternative operators:

`((temperature AND air) OR (pressure AND air) OR air) AND NOT sea`

Or:

`((temperature && air) || (pressure && air) || air) && !sea`

Boolean operators can be used on single terms, double-quotes enclosed phrases, and with sub-queries denoted with parentheses to form more complicated logical queries. 

### Fuzziness Matching
Any term can have the "fuzziness" operator -- `~` -- appended to it to utilize the [Damerau-Levenshtein distance](http://en.wikipedia.org/wiki/Damerau-Levenshtein_distance) for single character changes (insertion, deletion, substitution, or transposition). Using just the tilde defaults to a maximum of two changes, but an integer can be added after the tilde to specify the maximum number of changes that can be permitted for a successful match.

For example:

`volcano~` would match `volcanic` with the substitution of "i" for the second "o" (1 change) and the addition of a "c" (2 changes)

By default, terms are always required to match exactly as provided so if you're not sure on your spelling of a word make sure to include the fuzziness operator. Likewise, this can be helpful if you want to match a pluralized word, e.g., `ocean~` would match "ocean" and "oceans".

### Proximity Searches
Proximity searches can be thought of as fuzziness matching for terms. They enable a phrase match with terms being in a different order or with additional words between them. The default behavior of the query parsing has a distance of 0 between words in a phrase, meaning a phrase is not considered a match unless it appears exactly as written in the metadata document. Maximum distance can be specified, however, like so:

`"sea temperature"~3`

where matches would be found if the metadata text contains up to 3 words between `sea` and `temperature`. The closer the terms of a phrase are in a document, the higher that document would score in the returned results. Therefore, a document matching `sea temperature` exactly will appear before a document matching `sea surface temperature` in the results list.


### Boosting Search Clause Relevance
The relevance of a term, phrase, or group can be adjusted to your preference using the boost operator `^` and a number by which to adjust the weight. By default every element of your query has equal weight, which means a boost value of 1. A value between 0 and 1 reduces the weight of a clause whereas a value greater than 1 increases its weight.

For example:

`solar imagery^4 ultraviolet^2`


### Search By Field Name
While the default behavior of the query is to search against a limited set of fields, you can customize the fields you want to search against by specifying them in your query string. 

To search fields, simply precede your query text with the field name and a colon, as shown in the following examples:

- `title:octopus` 
- `description:(sonar OR lidar)`
- `doi:"10.1109/5.771073"`

#### Search Fields Requiring An Exact Match With An Inexact Query
To search multiple fields at once, just add them to your query string with parentheses around your clauses. Without any boolean operators, keep in mind that you have a default OR between field clauses.

`title:(octopus) description:(sonar OR lidar)`

For OneStop specifically, there are three different types of fields in the index: free-text searchable fields, exact-match searchable fields, and non-searchable fields. For free-text fields, you can use any of the operators that are in this guide for fine-tuning your search in a case-insensitive manner. On the other hand, exact-match fields _are_ case sensitive and the content of the field must match your query exactly in order for that record to be returned. Usage of [regular expressions](#regular-expressions) enable patterns in lieu of verbatim text for finding exact matches in these types of fields. Finally, non-searchable fields exist to provide additional information about results but have very little benefit for searchability, for example URL strings.

Information on all fields for a running version of OneStop can be acquired by sending a `GET` request to the OneStop [Search API](search-api) endpoint, for example `{host}/docs/attributes/{type}`

### Reserved characters
Since we pass the query straight through to elasticsearch, there are some special characters that elasticsearch treats differently. 
If you wish for elasticsearch query to ignore these characters then surround your query in double quotes.

https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html

Example:
- `title:"AFSC/RACE/GAP/McConnaughey:USBL"`

### Wildcards
A wildcard can be placed in individual terms for one character -- `?` -- or zero or more characters -- `*`. OneStop has disabled the use of a wildcard at the start of your query, and will respond with an error if you attempt to do so via either the UI or API. Wildcard queries can be particularly memory intensive and thus perform poorly if written in such a way that causes every record to be analyzed. As such, it is very much encouraged that they are used judiciously. Oftentimes you can have greater success in both response time and result quality by using [fuzziness](#fuzziness-matching) or [proximity](#proximity-searches) operators instead, or at the very least ensuring you've filtered down your results to a reasonable quantity (low thousands or less) before including a wildcard in your query.

### Regular Expressions
Regular expressions can be used in the query string to search by pattern on [specific fields](#search-by-field-name) or against the [default subset of fields](#default-query-behavior). We'll cover the basics to get you started and provide some real-world examples here. For further reading, you can find a link to Elasticsearch's regex operators overview at the bottom of this section.

To start out, a regular expression needs to be enclosed in forward slashes --  `/` -- in order to be treated as such. This is the case regardless of whether you are searching the default fields or an explicitly declared field. Next, if you are familiar with regular expressions, you may know that the beginning and end of a string must be anchored with `^` and `$`, respectively. This is **not** the case with the underlying Lucene regular expression engine, however, as Lucene's patterns are **always anchored**. Thus **the pattern provided must match the entire string**. Aside from these two notes, most of the regex operators supported here are common. 

To demonstrate the potential of regular expressions in a OneStop query, let's step through a real-world example of using them to find multiple collections with known `fileIdentifier` formats:

NOAA's Office of Ocean Exploration and Research (OER) creates a lot of [videos](https://www.youtube.com/oceanexplorergov) during their expeditions that are then stored and made discoverable through both [OER's data access products](https://oceanexplorer.noaa.gov/data/access/access.html) and the official [NOAA OneStop site](https://data.noaa.gov/onestop/). For this example, we'll assume that OER video data can be found in collections that have the following two formats:

Okeanors Explorer cruises: `{cruiseId}_DIVE##_{timestamp}_{keywords}` 

and 

All other cruises: `{cruiseId}_VID_{date}_{dive_Number}_{camera_Code}_TAPE{tape_Number}OF{number_Of_Tapes}_SEG{segment_Number}OF{total_Number_Of_Segments}.MP4`

where the fileIdentifier is fully capitalized.

In order to narrow down OneStop data to OER video collections by "cruiseId", you can do any of the following:

- If one cruiseId is given (exact match for the cruiseId value), all vessels:
  - `fileIdentifier:/cruiseId.*/`
  - Example: `fileIdentifier:/EX1902.*/` 
  - Example: `fileIdentifier:/EX1903L2.*/`

- If more than one cruiseId is given (exact matches), all vessels (separate each cruiseId with a pipe symbol):
  - `fileIdentifier:/cruiseId1.*|cruiseId2.*/`
  - Example: `fileIdentifier:/EX1902.*|EX1903L2.*/`

- If cruiseId is not given (this regex allows for any length ship name, followed by any number of digits, with an optional L and optional any number of digits to specify the leg #), all vessels:
  - `fileIdentifier:/[A-Z]+[0-9]+L?[0-9]*.*/`

- Searching for only Okeanos Explorer cruises (any number of digits after the ship abbreviation OR exactly 4 digits):
  - `fileIdentifier:/EX[0-9]+L?[0-9]*.*/`
  - `fileIdentifier:/EX[0-9]{4}L?[0-9]*.*/`

- Searching for only _non_ Okeanos Explorer cruises (where fileIdentifier has any ending OR it ends with “.MP4”, which we are assuming here no Okeanos Explorer fileIdentifiers do):
  - `fileIdentifier:/([A-Z]+[0-9]+L?[0-9]*.*)&~(EX[0-9]+L?[0-9]*.*)/`   
  - `fileIdentifier:/[A-Z]+[0-9]+L?[0-9]*.*\.MP4/`


As you can see, regular expressions are incredibly powerful tools in your searching toolbox. For an overview of all of the available regex operators, see Elasticsearch's guide on [Regular Expression Syntax](https://www.elastic.co/guide/en/elasticsearch/reference/current/regexp-syntax.html).


## Requesting Help
If you're unsure of something pertaining to the query syntax, feel free to search through existing issues in our [Feedback repository on GitHub](https://github.com/cedardevs/feedback/issues?utf8=%E2%9C%93&q=is%3Aissue) or open a [new issue](https://github.com/cedardevs/feedback/issues/new?assignees=&labels=help&template=help-request.md&title=Help+Request+--+) if you cannot find what you're looking for.


<hr>
<div align="center"><a href="#">Top of Page</a></div>