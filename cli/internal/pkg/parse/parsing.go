package parse

import (
	"github.com/cedardevs/onestop/cli/internal/pkg/flags"
	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
	"strconv"
	"strings"
	"time"
)

func ParseTypeFlag(params *viper.Viper) []string {
	facetFilter := []string{}
	typeArg := params.GetString(flags.TypeFlag)
	if len(typeArg) > 0 {
		// {"type":"collection","values":["88888888-8888-8888-8888-888888888888"]}
		facetFilter = []string{"{\"type\":\"collection\", \"values\":[\"" + typeArg + "\"]}"}
	}
	return facetFilter
}

func ParseSort(params *viper.Viper) string {
	// 	sortArg := params.GetString(flags.SortFlag)
	// 	sort := ""
	// 	if len(sortArg) > 0 {
	// 		sort = "\"sort\":[{\"" + sortArg + "\": \"desc\"}],"
	// 	}
	// 	return sort
	return "\"sort\":[{\"beginDate\": \"asc\", \"stagedDate\": \"asc\"}],"
}

func ParseChecksum(params *viper.Viper) []string {
	checksumFilter := []string{}
	checksumFlag := params.GetString(flags.ChecksumFlag)
	if len(checksumFlag) > 0 {
		checksumFilter = []string{"{\"type\":\"checksum\", \"algorithm\":\"SHA1\", \"values\":[\"" + checksumFlag + "\"]}"}
	}
	return checksumFilter
}

// func parseAvailableFlag(params *viper.Viper) []string {
// 	facetFilter := []string{}
// 	if params.GetString(AvailableFlag) == "true" {
// 		facetFilter = []string{"{\"type\":\"facet\",\"name\":\"dataCenters\",\"values\":[\"DOC/NOAA/NESDIS/STAR > Center for Satellite Applications and Research, NESDIS, NOAA, U.S. Department of Commerce\"]}"}
// 	}
// 	return facetFilter
// }

func ParseSatName(params *viper.Viper) []string {
	// {"type":"queryText", "value":"gcmdPlatforms:/GOES-16.*/"}
	satname := params.GetString(flags.SatnameFlag)
	querySatFilter := []string{}
	if len(satname) > 0 {
		querySatFilter = []string{"{\"type\":\"queryText\", \"value\":\"+gcmdPlatforms:/" + satname + ".*/\"}"}
	}
	return querySatFilter
}

func ParseRequestMeta(params *viper.Viper) string {
	requestMeta := ""
	maxPageSize := 1000
	pageSize := maxPageSize
	max, _ := strconv.Atoi(params.GetString(flags.MaxFlag))

	if max < maxPageSize {
		pageSize = max
	}
	searchAfter := params.GetString(flags.SearchAfterFlag)
	page := "\"page\" : {\"max\": " + strconv.Itoa(pageSize) + "}"
	requestMeta = page

	if len(searchAfter) > 0 {
		requestMeta = requestMeta + ", \"search_after\": [" + searchAfter + "]"
	}
	return requestMeta
}

//support for stime and start-time, same same
func ParseStartAndEndTime(params *viper.Viper) []string {
	filter := []string{}
	startTimeArg, endTimeArg := parseTimeFlags(params)
	beginDateTime, endDateTime := formatBeginAndEnd(startTimeArg, endTimeArg)
	beginDateTimeFilter, endDateTimeFilter := formatDateRange(beginDateTime, endDateTime)

	if len(beginDateTimeFilter) > 0 || len(endDateTimeFilter) > 0 {
		filter = []string{"{\"type\":\"datetime\", " + beginDateTimeFilter + endDateTimeFilter + "}"}
	}

	return filter
}

//support for stime and start-time, same same
func ParseSince(params *viper.Viper) []string {
	filter := []string{}
	startTime := params.GetString(flags.SinceFlag)
	if len(startTime) > 0 {
		beginDateTime, _ := time.Parse("2006-01-02T15:04:05Z", ParseDateFormat(startTime))
		beginDateTimeEpochMillis := beginDateTime.UnixNano() / 1000000
		beginDateTimeFilter := "stagedDate:>" + strconv.FormatInt(beginDateTimeEpochMillis, 10)
		if len(beginDateTimeFilter) > 0 {
			filter = []string{"{\"type\":\"queryText\", \"value\":\"" + beginDateTimeFilter + "\"}"}
		}
	}
	return filter
}

func parseTimeFlags(params *viper.Viper) (string, string) {
	startTime := params.GetString(flags.StartTimeFlag)
	if len(startTime) == 0 {
		startTime = params.GetString(flags.StartTimeScdrFlag)
	}
	endTime := params.GetString(flags.EndTimeFlag)
	if len(endTime) == 0 {
		endTime = params.GetString(flags.EndTimeScdrFlag)
	}
	return startTime, endTime
}

func formatBeginAndEnd(startTime string, endTime string) (string, string) {
	beginDateTime := ParseDateFormat(startTime)
	endDateTime := ParseDateFormat(endTime)
	return beginDateTime, endDateTime
}

func formatDateRange(beginDateTime string, endDateTime string) (string, string) {
	beginDateTimeFilter := ""
	endDateTimeFilter := ""
	if len(beginDateTime) > 0 {
		beginDateTimeFilter = "\"after\":\"" + beginDateTime + "\""
		if len(endDateTime) > 0 {
			beginDateTimeFilter = beginDateTimeFilter + ", "
		}
	}
	if len(endDateTime) > 0 {
		endDateTimeFilter = "\"before\":\"" + endDateTime + "\""
	}
	return beginDateTimeFilter, endDateTimeFilter
}

func ParseDateFormat(dateString string) string {

	supportedLayouts := []string{
		time.UnixDate,
		"2006-01-02",
		"2006-01-02 15:04",
		"2006-01-02 15:04:05",
		"2006-01-02 15:04:05 MST",
		"2006-01-02T15:04:05",
		"2006-01-02T15:04:05Z",
		"2006/01/02",
		"2006/01/02 15:04",
		"2006/01/02 15:04:05",
		"2006/01/02 15:04:05 MST",
		"January 2 2006 15:04",
		"January 2 2006 15:04:05",
		"Jan 2, 2006 at 3:04pm",
		"January 2st 2006", //this looks crazy, but is necessary to support ordinal indicators
		"January 2nd 2006",
		"January 2rd 2006",
		"January 2th 2006",
		"January 2 2006 15:04",
		"January 2st 2006 15:04",
		"January 2nd 2006 15:04",
		"January 2rd 2006 15:04",
		"January 2th 2006 15:04",
		"January 2 2006 15:04:05",
		"January 2st 2006 15:04:05",
		"January 2nd 2006 15:04:05",
		"January 2rd 2006 15:04:05",
		"January 2th 2006 15:04:05",
		"January 2 2006 at 15:04",
		"January 2st 2006 at 15:04",
		"January 2nd 2006 at 15:04",
		"January 2rd 2006 at 15:04",
		"January 2th 2006 at 15:04",
		"January 2 2006 at 15:04:05",
		"January 2st 2006 at 15:04:05",
		"January 2nd 2006 at 15:04:05",
		"January 2rd 2006 at 15:04:05",
		"January 2th 2006 at 15:04:05",
	}
	formattedDate := ""
	for _, layout := range supportedLayouts {
		if formattedDate == "" && len(dateString) > 0 {
			t1, err1 := time.Parse(layout, dateString)
			if err1 == nil {
				formattedDate = t1.UTC().Format("2006-01-02T15:04:05Z")
			}
		}
	}
	if len(formattedDate) < 0 {
		log.Fatal().Msg("Date syntax not supported- " + dateString)
	}
	return formattedDate
}

func ParseDate(params *viper.Viper) []string {
	//parse date flags, add filter
	date := params.GetString(flags.DateFilterFlag)
	if len(date) == 0 {
		return []string{}
	}
	beginDateTime := ParseDateFormat(date)
	if len(beginDateTime) == 0 {
		currentTime := time.Now() //support for current year default
		beginDateTime = ParseDateFormat(strconv.Itoa(currentTime.Year()) + "/" + date)
		if len(beginDateTime) == 0 { //stupid way to check for format "/" or "-"
			beginDateTime = ParseDateFormat(strconv.Itoa(currentTime.Year()) + "-" + date)
		}
	}
	t2, _ := time.Parse("2006-01-02T00:00:00Z", beginDateTime)
	endDateTime := t2.AddDate(0, 0, 1).Format("2006-01-02T00:00:00Z")
	return []string{"{\"type\":\"datetime\", \"after\":\"" + beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
}

func ParseFileName(params *viper.Viper) []string {
	fileName := params.GetString(flags.FileFlag)
	if len(fileName) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"title:\\\"" + fileName + "\\\"\"}"}
}

func ParseMonth(params *viper.Viper) []string {
	month := params.GetString(flags.MonthFlag)
	if len(month) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"beginMonth:" + month + "\"}"}
}

func ParseDayOfMonth(params *viper.Viper) []string {
	dom := params.GetString(flags.DayFlag)
	if len(dom) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"beginDayOfMonth:" + dom + "\"}"}
}

func ParseDayOfYear(params *viper.Viper) []string {
	doy := params.GetString(flags.DoyFlag)
	if len(doy) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"beginDayOfYear:" + doy + "\"}"}
}

func ParseYear(params *viper.Viper) []string {
	year := params.GetString(flags.YearFlag)
	if len(year) == 0 {
		return []string{}
	}
	beginDate := year + "-01-01"
	beginDateTime := ParseDateFormat(beginDate)
	t2, _ := time.Parse("2006-01-02T00:00:00Z", beginDateTime)
	endDateTime := t2.AddDate(1, 0, 0).Format("2006-01-02T00:00:00Z")
	return []string{"{\"type\":\"datetime\", \"after\":\"" + beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
}

func ParseRegexFileName(params *viper.Viper) []string {
	regex := params.GetString(flags.ReFileFlag)
	if len(regex) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"title:" + regex + "\"}"}
}

func ParseTextQuery(params *viper.Viper) []string {
	query := params.GetString(flags.TextQueryFlag)
	if len(query) == 0 {
		query = params.GetString(flags.MetadataFlag)
	}
	if len(query) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"" + query + "\"}"}
}

func ParsePolygon(params *viper.Viper) []string {
	polygon := params.GetString(flags.SpatialFilterFlag)
	if len(polygon) == 0 {
		return []string{}
	}
	polygon = strings.ReplaceAll(polygon, "POLYGON((", "")
	polygon = strings.ReplaceAll(polygon, "))", "")
	coords := strings.Split(polygon, ",")
	geospatialFilter := []string{}
	for i := 0; i < len(coords); i++ {
		coords[i] = strings.TrimSpace(coords[i])
		coords[i] = strings.ReplaceAll(coords[i], " ", ",")
		coord := strings.Split(coords[i], ",")
		end := ", "
		if i+1 == len(coords) {
			end = ""
		}
		geospatialFilter = append(geospatialFilter, "["+coord[0]+","+coord[1]+"]"+end)
	}
	return []string{"{\"geometry\": { \"coordinates\": [[" + strings.Join(geospatialFilter, "") + "]], \"type\": \"Polygon\"}, \"type\": \"geometry\"}"}
}

func ParseKeyword(params *viper.Viper) []string {
	keyword := params.GetString(flags.KeywordFlag)
	if len(keyword) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\",\"value\":\"+keywords:\\\"" + keyword + "\\\"\"}"}
}
