package main

const searchCollectionCmd = "searchcollection"
const searchGranuleCmd = "searchgranule"
const searchFlattenedGranuleCmd = "searchflattenedgranule"

const cloudServerFlag = "cloud"
const cloudServerShortFlag = "c"
const cloudServerDescription = "Use cloud intance."

const testServerFlag = "test"
const testServerShortFlag = "u"
const testServerDescription = "Use test intance."

const textQueryFlag = "query"
const textQueryShortFlag = "q"
const queryDescription = "Search flattened granules with text query"

const dateFilterFlag = "date"
const dateFilterShortFlag = "d"
const dateDescription = "DATE must be a string describing a date. Current year is assumed if the year part in DATE is ommited, e.g. 01-30. Any time information in DATE is disregarded. Files with data between DATE midnight and next day midnight are selected."

const spatialFilterFlag = "area"
const spatialFilterShortFlag = "g" //as in geometry
const areaDescription = "Locate files which intersect with the specified polygon AREA. The polygon must be a regular one (closed, no self-intersection, no hole) with coordinates (longitude, latitude) separated by \",\" like POLYGON((30.31 60.2, 31.21 60.2, 31.21 60.76, 30.31 60.76, 30.31 60.2)) "

const maxFlag = "max"
const maxShortFlag = "l"
const maxDescription = "Maximum number of results returned."

const offsetFlag = "offset"
const offsetShortFlag = "p" //as in page
const offsetDescription = "Page number starting at 0"

const startTimeFlag = "start-time"
const startTimeShortFlag = "s"
const startTimeDescription = "Match files occurring on or after this date."

const startTimeScdrFlag = "stime"
const startTimeScdrDescription = "Same behavior as start-time, but short hand flag for scdr files."

const endTimeFlag = "end-time"
const endTimeShortFlag = "e"
const endTimeDescription = "Match files occurring on or before this date."

const endTimeScdrFlag = "etime"
const endTimeScdrDescription = "Same behavior as end-time, but short hand flag for scdr files."

const availableFlag = "available"
const availableShortFlag = "a"
const availableDescription = "Searches OneStop for files associated with [COLLECTION]"

const metadataFlag = "metadata"
const metadataShortFlag = "m"
const metadataDescription = "Text search against available metadata."

const typeFlag = "type"
const typeShortFlag = "t"

// const regexFileCmd = "re-file"
const typeDescription = "Search only for files of the specified data collection using the collection's file identfier. Using this option is highly recommended for any kind of file searches. Collection identifiers are case sensitive."

const fileFlag = "file"
const fileShortFlag = "f"
const fileFlagDescription = "Locate files with exact FILENAME."

const refileFlag = "re-file"
const refileShortFlag = "r"
const regexDescription = "Locate files whose names match the case-insensitive regular expression REGEX. Only one regular expression is allowed, not longer than 100 characters."

const satnameFlag = "satname"
const satnameDescription = "Select files from the SATNAME satellite."


const yearFlag = "year"
const yearShortFlag = "y"
const yearDescription = "Specifies a year. YEAR must be a positive integer, ranging between 1978 and current year. Files with data start times in that year will be listed."
