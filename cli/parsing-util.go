package main

import (
	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strconv"
	"strings"
	"time"
)

//this function is pre-request RegisterBefore
func parseOneStopRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {
	filters := []string{}
	queries := []string{}

	dateTimeFilter := parseDate(params)
	filters = append(filters, dateTimeFilter...)
	startEndTimeFilter := parseStartAndEndTime(params)
	filters = append(filters, startEndTimeFilter...)
	geoSpatialFilter := parsePolygon(params)
	filters = append(filters, geoSpatialFilter...)
	query := parseTextQuery(params)
	queries = append(queries, query...)
	requestMeta := parseRequestMeta(params)
	if len(queries) > 0 || len(filters) > 0 {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
	}
}

func parseScdrRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {

	//apply a default filter for STAR
	filters := []string{"{\"type\":\"facet\",\"name\":\"dataCenters\",\"values\":[\"DOC/NOAA/NESDIS/STAR > Center for Satellite Applications and Research, NESDIS, NOAA, U.S. Department of Commerce\"]}"}
	queries := []string{}

	// isSummaryWithType := params.GetString(availableFlag) == "true" && len(params.GetString("type")) > 0

	collectionIdFilter := parseTypeFlag(params)
	filters = append(filters, collectionIdFilter...)
	// datacenterFilter := parseAvailableFlag(params)
	// filters = append(filters, datacenterFilter...)
	dateTimeFilter := parseDate(params)
	filters = append(filters, dateTimeFilter...)
	yearFilter := parseYear(params)
	filters = append(filters, yearFilter...)
	startEndTimeFilter := parseStartAndEndTime(params)
	filters = append(filters, startEndTimeFilter...)
	geoSpatialFilter := parsePolygon(params)
	filters = append(filters, geoSpatialFilter...)

	satnameQuery := parseSatName(params)
	queries = append(queries, satnameQuery...)
	fileNameQuery := parseFileName(params)
	queries = append(queries, fileNameQuery...)
	refileNameQuery := parseRegexFileName(params)
	queries = append(queries, refileNameQuery...)
	query := parseTextQuery(params)
	queries = append(queries, query...)
	keyWordFilter := parseKeyword(params)
	queries = append(queries, keyWordFilter...)
	requestMeta := parseRequestMeta(params)

	if len(queries) > 0 || len(filters) > 0 {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"summary\":false, \"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
	}

}

func parseTypeFlag(params *viper.Viper) []string {
	facetFilter := []string{}
	typeArg := params.GetString(typeFlag)
	if len(typeArg) > 0 {
		// {"type":"collection","values":["88888888-8888-8888-8888-888888888888"]}
		facetFilter = []string{"{\"type\":\"collection\", \"values\":[\"" + typeArg + "\"]}"}
	}
	return facetFilter
}

// func parseAvailableFlag(params *viper.Viper) []string {
// 	facetFilter := []string{}
// 	if params.GetString(availableFlag) == "true" {
// 		facetFilter = []string{"{\"type\":\"facet\",\"name\":\"dataCenters\",\"values\":[\"DOC/NOAA/NESDIS/STAR > Center for Satellite Applications and Research, NESDIS, NOAA, U.S. Department of Commerce\"]}"}
// 	}
// 	return facetFilter
// }

func parseSatName(params *viper.Viper) []string {
	// {"type":"queryText", "value":"gcmdPlatforms:/GOES-16.*/"}
	satname := params.GetString(satnameFlag)
	querySatFilter := []string{}
	if len(satname) > 0 {
		querySatFilter = []string{"{\"type\":\"queryText\", \"value\":\"gcmdPlatforms:/" + satname + ".*/\"}"}
	}
	return querySatFilter
}

func parseRequestMeta(params *viper.Viper) string {
	max := params.GetString(maxFlag)
	offset := params.GetString(offsetFlag)
	if len(max) == 0 {
		max = "100"
	}
	if len(offset) == 0 {
		offset = "0"
	}
	page := "\"page\" : {\"max\": " + max + ", \"offset\": " + offset + "}"
	return page
}

//support for stime and start-time, same same
func parseStartAndEndTime(params *viper.Viper) []string {
	filter := []string{}
	startTimeArg, endTimeArg := parseTimeFlags(params)
	beginDateTime, endDateTime := formatBeginAndEnd(startTimeArg, endTimeArg)
	beginDateTimeFilter, endDateTimeFilter := formatDateRange(beginDateTime, endDateTime)

	if len(beginDateTimeFilter) > 0 || len(endDateTimeFilter) > 0 {
		filter = []string{"{\"type\":\"datetime\", " + beginDateTimeFilter + endDateTimeFilter + "}"}
	}

	return filter
}

func parseTimeFlags(params *viper.Viper) (string, string) {
	startTime := params.GetString(startTimeFlag)
	if len(startTime) == 0 {
		startTime = params.GetString(startTimeScdrFlag)
	}
	endTime := params.GetString(endTimeFlag)
	if len(endTime) == 0 {
		endTime = params.GetString(endTimeScdrFlag)
	}
	return startTime, endTime
}

func formatBeginAndEnd(startTime string, endTime string) (string, string) {
	beginDateTime := parseDateFormat(startTime)
	endDateTime := parseDateFormat(endTime)
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

func parseDateFormat(dateString string) string {

	supportedLayouts := []string{
		time.UnixDate,
		"2006-01-02",
		"2006-01-02 15:04",
		"2006-01-02 15:04:05",
		"2006-01-02 15:04:05 MST",
		"2006/01/02",
		"2006/01/02 15:04",
		"2006/01/02 15:04:05",
		"2006/01/02 15:04:05 MST",
		"January 2 2006 15:04",
		"January 2 2006 15:04:05",
		"Jan 2, 2006 at 3:04pm",
		"January 2st 2006",
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

func parseDate(params *viper.Viper) []string {
	//parse date flags, add filter
	date := params.GetString(dateFilterFlag)
	if len(date) == 0 {
		return []string{}
	}
	beginDateTime := parseDateFormat(date)
	if len(beginDateTime) == 0 {
		currentTime := time.Now() //support for current year default
		beginDateTime = parseDateFormat(strconv.Itoa(currentTime.Year()) + "/" + date)
		if len(beginDateTime) == 0 { //stupid way to check for format "/" or "-"
			beginDateTime = parseDateFormat(strconv.Itoa(currentTime.Year()) + "-" + date)
		}
	}
	t2, _ := time.Parse("2006-01-02T00:00:00Z", beginDateTime)
	endDateTime := t2.AddDate(0, 0, 1).Format("2006-01-02T00:00:00Z")
	return []string{"{\"type\":\"datetime\", \"after\":\"" + beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
}

func parseFileName(params *viper.Viper) []string {
	fileName := params.GetString(fileFlag)
	if len(fileName) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"title:\\\"" + fileName + "\\\"\"}"}
}

func parseYear(params *viper.Viper) []string {
	year := params.GetString(yearFlag)
	if len(year) == 0 {
		return []string{}
	}
	beginDate := year + "-01-01"
	beginDateTime := parseDateFormat(beginDate)
	t2, _ := time.Parse("2006-01-02T00:00:00Z", beginDateTime)
	endDateTime := t2.AddDate(1, 0, 0).Format("2006-01-02T00:00:00Z")
	return []string{"{\"type\":\"datetime\", \"after\":\"" + beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
}

func parseRegexFileName(params *viper.Viper) []string {
	regex := params.GetString(refileFlag)
	if len(regex) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"title:\\\\\\\"/" + regex + "/\\\\\\\"\"}"}
}

func parseTextQuery(params *viper.Viper) []string {
	query := params.GetString(textQueryFlag)
	if len(query) == 0 {
		query = params.GetString(metadataFlag)
	}
	if len(query) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"" + query + "\"}"}
}

func parsePolygon(params *viper.Viper) []string {
	polygon := params.GetString(spatialFilterFlag)
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

func parseKeyword(params *viper.Viper) []string {
	keyword := params.GetString(keywordFlag)
	if len(keyword) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\",\"value\":\"+keywords:\\\"" + keyword + "\\\"\"}"}
}
