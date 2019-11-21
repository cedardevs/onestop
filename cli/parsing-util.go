package main

import (
	"github.com/rs/zerolog/log"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strconv"
	"strings"
	"time"
)

func parseOneStopRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {
	filters := []string{}
	queries := []string{}

	dateTimeFilter := parseDateArgs(params)
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
	filters := []string{}
	queries := []string{}

	isSummaryWithType := params.GetString("available") == "true" && len(params.GetString("type")) > 0

	if isSummaryWithType { // then it's a GET, not a POST
		req.BodyString("")
	} else {
		dataCenterFacetFilter := parseAvailableFlag(params)
		filters = append(filters, dataCenterFacetFilter...)
		dateTimeFilter := parseDateArgs(params)
		filters = append(filters, dateTimeFilter...)
		startEndTimeFilter := parseStartAndEndTime(params)
		filters = append(filters, startEndTimeFilter...)
		geoSpatialFilter := parsePolygon(params)
		filters = append(filters, geoSpatialFilter...)
		parentIdentifierQuery := parseParentIdentifier(params)
		queries = append(queries, parentIdentifierQuery...)
		fileIdentifierQuery := parseFileIdentifier(params)
		queries = append(queries, fileIdentifierQuery...)
		regexQuery := parseParentIdentifierRegex(params)
		queries = append(queries, regexQuery...)
		query := parseTextQuery(params)
		queries = append(queries, query...)
		requestMeta := parseRequestMeta(params)

		if len(queries) > 0 || len(filters) > 0 {
			req.AddHeader("content-type", "application/json")
			req.BodyString("{\"summary\":false, \"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
		} else {
			req.AddHeader("content-type", "application/json")
			req.BodyString("{\"summary\":false, " + requestMeta + "}")
		}
	}
}

func parseAvailableFlag(params *viper.Viper) []string {
	facetFilter := []string{}
	if params.GetString(availableFlag) == "true" {
		facetFilter = []string{"{\"type\":\"facet\",\"name\":\"dataCenters\",\"values\":[\"DOC/NOAA/NESDIS/STAR > Center for Satellite Applications and Research, NESDIS, NOAA, U.S. Department of Commerce\"]}"}
	}
	return facetFilter
}

func parseRequestMeta(params *viper.Viper) string {
	max := params.GetString("max")
	offset := params.GetString("offset")
	if len(max) == 0 {
		max = "100"
	}
	if len(offset) == 0 {
		offset = "0"
	}
	page := "\"page\" : {\"max\": " + max + ", \"offset\": " + offset + "}"
	return page
}

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
	startTime := params.GetString("start-time")
	if len(startTime) == 0 {
		startTime = params.GetString("stime")
	}
	endTime := params.GetString("end-time")
	if len(endTime) == 0 {
		endTime = params.GetString("etime")
	}
	return startTime, endTime
}

func formatBeginAndEnd(startTime string, endTime string) (string, string) {
	beginDateTime := parseDate(startTime)
	endDateTime := parseDate(endTime)
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

func parseDate(dateString string) string {

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

func parseDateArgs(params *viper.Viper) []string {
	//parse date flags, add filter
	date := params.GetString("date")
	if len(date) == 0 {
		return []string{}
	}
	beginDateTime := parseDate(date)
	if len(beginDateTime) == 0 {
		currentTime := time.Now() //support for current year default
		beginDateTime = parseDate(strconv.Itoa(currentTime.Year()) + "/" + date)
		if len(beginDateTime) == 0 {
			beginDateTime = parseDate(strconv.Itoa(currentTime.Year()) + "-" + date)
		}
	}
	t2, _ := time.Parse("2006-01-02T00:00:00Z", beginDateTime)
	endDateTime := t2.AddDate(0, 0, 1).Format("2006-01-02T00:00:00Z")
	return []string{"{\"type\":\"datetime\", \"after\":\"" + beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
}

func parseParentIdentifier(params *viper.Viper) []string {
	parentId := params.GetString("type")
	if len(parentId) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"parentIdentifier:\\\"" + parentId + "\\\"\"}"}
}

func parseFileIdentifier(params *viper.Viper) []string {
	fileId := params.GetString("file")
	if len(fileId) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"fileIdentifier:\\\"" + fileId + "\\\"\"}"}
}

func parseParentIdentifierRegex(params *viper.Viper) []string {
	regex := params.GetString("re-file")
	if len(regex) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"parentIdentifier:" + regex + "\"}"}
}

func parseTextQuery(params *viper.Viper) []string {
	query := params.GetString("query")
	if len(query) == 0 {
		query = params.GetString("metadata")
	}
	if len(query) == 0 {
		return []string{}
	}
	return []string{"{\"type\":\"queryText\", \"value\":\"" + query + "\"}"}
}

func parsePolygon(params *viper.Viper) []string {
	polygon := params.GetString("area")
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
