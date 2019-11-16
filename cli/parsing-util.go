package main

import (
	"github.com/spf13/viper"
	"strconv"
	"time"
	"strings"
  "github.com/rs/zerolog/log"
  "gopkg.in/h2non/gentleman.v2"
)


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
  if len(queries) > 0 || len(filters) > 0  {
		req.AddHeader("content-type", "application/json")
    req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
  }
}

func parseScdrRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {
	filters := []string{}
	queries := []string{}

	dateTimeFilter := parseDate(params)
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

	if len(queries) > 0 || len(filters) > 0  {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "],"+ requestMeta +"}")
	}
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

//TODO: WIP - this needs work
func parseStartAndEndTime(params *viper.Viper) []string {

  startTime, endTime := parseTimeFlags(params)

	if len(startTime) == 0 && len(endTime) == 0 {
		return []string{}
	}

	layout1 := "2006-01-02"
	layout2 := "2003-01-02 00:00"
	layout3 := "January 2 2006 15:04:05"
	layout4 := "January 2nd 2006"
	layout5 := "Jan 2, 2006 at 3:04pm"
	// "Jan 2, 2006 at 3:04pm (MST)"
	supportedLayouts := []string{layout1, layout2, layout3, layout4, layout5}

	beginDateTime := ""
	endDateTime := ""

	for _, layout := range supportedLayouts {
		if beginDateTime == "" && len(startTime) > 0 {
			t1, err1 := time.Parse(layout, startTime)
			if err1 == nil {
				// log.Info().Msg(t1.String())
				beginDateTime = t1.Format("2006-01-02T15:04:05Z")
			} else {
				// fmt.Printf("error: for %q got %q; expected %q\n", startTime, t1, layout)
				// log.Info().Err(err1).Msg("Date syntax not supported.")
				// log.Fatal().Err(err1).Msg("Date syntax not supported.")
			}
		}
		if endDateTime == "" && len(endTime) > 0 {
			t2, err2 := time.Parse(layout, endTime)
			if err2 == nil {
				endDateTime = t2.Format("2006-01-02T00:00:00Z")
			} else {
				// log.Info().Err(err2).Msg("Date syntax not supported.")

				// log.Fatal().Err(err2).Msg("Date syntax not supported.")
			}
		}
	}

	beginDateTimeFilter := ""
	endDateTimeFilter := ""
	relation := ""

	if len(startTime) > 0 {
		beginDateTimeFilter = "\"after\":\"" + beginDateTime + "\""
		if len(endTime) > 0 {
			beginDateTimeFilter = beginDateTimeFilter + ", "
		}
	}

	if len(endTime) > 0 {
		endDateTimeFilter = "\"before\":\"" + endDateTime + "\""
	}

	return []string{"{\"type\":\"datetime\", " + relation + beginDateTimeFilter + endDateTimeFilter + "}"}
}

func parseDate(params *viper.Viper) []string {
	//parse date flags, add filter
	date := params.GetString("date")
	if len(date) == 0 {
		return []string{}
	}
	layout := "2006/01/02"
	t, err := time.Parse(layout, date)
	if err != nil {
		currentTime := time.Now() //support for current year default
		t, err = time.Parse(layout, strconv.Itoa(currentTime.Year()) + "/" + date)
		if err != nil {
      log.Fatal().Err(err).Msg("Date syntax not supported.")
		}
	}
	beginDateTime := t.Format("2006-01-02T00:00:00Z")
	t2 := t.AddDate(0,0,1)
  endDateTime := t2.Format("2006-01-02T00:00:00Z")
	return []string{"{\"type\":\"datetime\", \"after\":\""+ beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
}

func parseParentIdentifier(params *viper.Viper) []string {
	parentId := params.GetString("type")
	if len(parentId) <= 0 {
		parentId = params.GetString("available")
	}
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
		if i + 1 == len(coords){
			end = ""
		}
		geospatialFilter = append(geospatialFilter, "[" + coord[0] + "," + coord[1] +"]" + end)
	}
	return []string{"{\"geometry\": { \"coordinates\": [[" + strings.Join(geospatialFilter, "") + "]], \"type\": \"Polygon\"}, \"type\": \"geometry\"}"}
}
