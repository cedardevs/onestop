package flags

const SearchCollectionCmd = "searchcollection"
const SearchGranuleCmd = "searchgranule"
const SearchFlattenedGranuleCmd = "searchflattenedgranule"

const CloudServerFlag = "cloud"
const CloudServerShortFlag = "c"
const CloudServerDescription = "Use cloud intance. Warning: --cloud will be replace with --server on next release"

const TestServerFlag = "test"
const TestServerShortFlag = "u"
const TestServerDescription = "Use sciapps intance. Warning: --test will be replace with --server on next release"

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
const MaxDefault = 10000

const OffsetFlag = "offset"
const OffsetShortFlag = "p" //as in page
const OffsetDescription = "Page number starting at 0"
const OffsetDefault = 0

const SearchAfterFlag = "after"
const SearchAfterShortFlag = ""
const SearchAfterDescription = "Search after begin date."

// const SearchAfterDefault = 0

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

const MonthFlag = "month"
const MonthShortFlag = ""
const MonthDescription = "Specifies a month of a year. MONTH must be a positive integer, ranging between 1 and 12. Files with data start times from any day of that month for any year will be listed."

const DoyFlag = "doy"
const DoyShortFlag = ""
const DoyDescription = "Specifies the day of the year. DOY must be an integer, ranging between 1 and 366. Files with data start times on that day of any year will be listed."

const DayFlag = "day"
const DayShortFlag = ""
const DayDescription = "Specifies a day of a month. DAY must be a positive integer, ranging between 1 and 31. Files with data start times on that day of any month and year will be selected."

const YearFlag = "year"
const YearShortFlag = "y"
const YearDescription = "Specifies a year. YEAR must be a positive integer, ranging between 1978 and current year. Files with data start times in that year will be listed."

const KeywordFlag = "label"
const KeywordShortFlag = "l"
const KeywordDescription = "Pick files tagged with a label whose value is LABEL. Files can be tagged with more than one label. Satellite names (see option --satname) are just one type of labels."

//not used
// const SortFlag = "sort"
// const SortShortFlag = ""
// const SortDefault = "beginDate"
// const SortDescription = "Sort results by stagedDate, beginDate, or endDate."
const CloudFlag = "cloud"
const CloudUrl = "http://acf3425c8d41b11e9a12912cf37a7528-1694331899.us-east-1.elb.amazonaws.com/onestop-search"

const GapFlag = "gap"
const GapShortFlag = "i"
const GapDescription = "Defines the time interval for reporting data gaps. Any data time gap larger than a specified interval will be reported. Valid time units are  \"h\", \"m\", \"s\", \"ms\", \"ns\", \"us\" (or \"µs\"), e.g. 1h30m0.5s This option is only meaningful when combined with the --type option. It is silently ignored if --available is passed or no --type is provided. --gap INTERVAL"

const SinceFlag = "since"
const SinceShortFlag = ""
const SinceDescription = "Select files who are added to the repository since DATETIME. DATETIME is a string specifying both a date and a time. Current year is assumed if the year cannot be inferred from the DATETIME string. If the time part is missing, the time \"00:00:00\" is assumed. ISO 8601 time format with time zone is strongly recommended for DATETIME values. --since DATETIME"

const ChecksumFlag = "sha1"
const ChecksumShortFlag = ""
const ChecksumDescription = "Locate files whose content’s SHA-1 value is CHECKSUM, --sha1 CHECKSUM"
