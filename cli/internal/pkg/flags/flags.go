package flags

const SearchCollectionCmd = "searchcollection"
const SearchGranuleCmd = "searchgranule"
const SearchFlattenedGranuleCmd = "searchflattenedgranule"

const CloudServerFlag = "cloud"
const CloudServerShortFlag = "c"
const CloudServerDescription = "Use cloud intance."

const TestServerFlag = "test"
const TestServerShortFlag = "u"
const TestServerDescription = "Use test intance."

const TextQueryFlag = "query"
const TextQueryShortFlag = "q"
const QueryDescription = "Search flattened granules with text query"

const DateFilterFlag = "date"
const DateFilterShortFlag = "d"
const DateDescription = "DATE must be a string describing a date. Current year is assumed if the year part in DATE is ommited, e.g. 01-30. Any time information in DATE is disregarded. Files with data between DATE midnight and next day midnight are selected."

const SpatialFilterFlag = "area"
const SpatialFilterShortFlag = "g" //as in geometry
const AreaDescription = "Locate files which intersect with the specified polygon AREA. The polygon must be a regular one (closed, no self-intersection, no hole) with coordinates (longitude, latitude) separated by \",\" like POLYGON((30.31 60.2, 31.21 60.2, 31.21 60.76, 30.31 60.76, 30.31 60.2)) "

const MaxFlag = "max"
const MaxShortFlag = "n"
const MaxDescription = "Maximum number of results returned."

const OffsetFlag = "offset"
const OffsetShortFlag = "p" //as in page
const OffsetDescription = "Page number starting at 0"

const StartTimeFlag = "start-time"
const StartTimeShortFlag = "s"
const StartTimeDescription = "Match files occurring on or after this date."

const StartTimeScdrFlag = "stime"
const StartTimeScdrDescription = "Same behavior as start-time, but short hand flag for scdr files."

const EndTimeFlag = "end-time"
const EndTimeShortFlag = "e"
const EndTimeDescription = "Match files occurring on or before this date."

const EndTimeScdrFlag = "etime"
const EndTimeScdrDescription = "Same behavior as end-time, but short hand flag for scdr files."

const AvailableFlag = "available"
const AvailableShortFlag = "a"
const AvailableDescription = "Searches OneStop for files associated with [COLLECTION]"

const MetadataFlag = "metadata"
const MetadataShortFlag = "m"
const MetadataDescription = "Text search against available metadata."

const TypeFlag = "type"
const TypeShortFlag = "t"

// const regexFileCmd = "re-file"
const TypeDescription = "Search only for files of the specified data collection using the collection's file identfier. Using this option is highly recommended for any kind of file searches. Collection identifiers are case sensitive."

const FileFlag = "file"
const FileShortFlag = "f"
const FileFlagDescription = "Locate files with exact FILENAME."

const ReFileFlag = "re-file"
const ReFileShortFlag = "r"
const RegexDescription = "Locate files whose names match the case-insensitive regular expression REGEX. Only one regular expression is allowed, not longer than 100 characters."

const SatnameFlag = "satname"
const SatnameDescription = "Select files from the SATNAME satellite."


const YearFlag = "year"
const YearShortFlag = "y"
const YearDescription = "Specifies a year. YEAR must be a positive integer, ranging between 1978 and current year. Files with data start times in that year will be listed."

const KeywordFlag = "label"
const KeywordShortFlag = "l"
const KeywordDescription = "Pick files tagged with a label whose value is LABEL. Files can be tagged with more than one label. Satellite names (see option --satname) are just one type of labels."

const CloudFlag = "cloud"
const CloudUrl = "http://acf3425c8d41b11e9a12912cf37a7528-1694331899.us-east-1.elb.amazonaws.com/onestop-search"

const GapFlag = "gap"
const GapShortFlag = "i"
const GapDescription = "Defines the time interval for reporting data gaps. Any data time gap larger than INTERVAL will be reported. This option is only meaningful when combined with either the --type options. It is silently ignored in all other cases."
