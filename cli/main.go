package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"crypto/tls"
	gtls "gopkg.in/h2non/gentleman.v2/plugins/tls"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"fmt"
	"strconv"
	"time"
	"strings"
)

func parseDate(params *viper.Viper) []string {
	//parse date flags, add filter
	date := params.GetString("date")
	if len(date) == 0 {
		return []string{}
	}
	layout := "2006-01-02"
	t, err := time.Parse(layout, date)
	if err != nil {
		fmt.Println(err) //change to log statements
		currentTime := time.Now() //support for current year default
		t, err = time.Parse(layout, strconv.Itoa(currentTime.Year()) + "-" + date)
		if err != nil {
			fmt.Println("Date syntax not supported.")
		}
	}
	beginDateTime := t.Format("2006-01-01T00:00:00Z")
	endDateTime := t.AddDate(0,0,1).Format("2006-01-01T00:00:00Z")

	return []string{"{\"type\":\"datetime\", \"after\" :\""+ beginDateTime + "\", \"before\":\"" + endDateTime + "\"}"}
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
	query := params.GetString("q")
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

func main() {
	cli.Init(&cli.Config{
		AppName:   "onestop-cli",
		EnvPrefix: "ONESTOP_CLI",
		Version:   "1.0.0",
	})

	cli.Client.Use(gtls.Config(&tls.Config{InsecureSkipVerify: true}))

    cli.AddFlag("scdr-files", "date", "", "DATE must be a string describing a date, e.g. 2010-01-30. Current year is assumed if the year part in DATE is ommited, e.g. 01-30. Any time information in DATE is disregarded. Files with data between DATE midnight and next day midnight are selected.", "")
	cli.AddFlag("scdr-files", "type", "", "Search only for files of the specified data collection using the collection's file identfier. Using this option is highly recommended for any kind of file searches. Collection identifiers are case sensitive.", "")
	cli.AddFlag("scdr-files", "area", "", "Locate files which intersect with the specified polygon AREA. The polygon must be a regular one (closed, no self-intersection, no hole) with coordinates (longitude, latitude) separated by \",\" like POLYGON((30.31 60.2, 31.21 60.2, 31.21 60.76, 30.31 60.76, 30.31 60.2)) ", "")
	cli.AddFlag("scdr-files", "re-file", "", "Locate files whose names match the case-insensitive regular expression REGEX. Only one regular expression is allowed, not longer than 100 characters.", "")
    cli.AddFlag("scdr-files", "q", "", "Search flattened granules with text query", "")

	cli.RegisterBefore("scdr-files", func(cmd string, params *viper.Viper, req *gentleman.Request) {
		filters := []string{}
		queries := []string{}

		dateTimeFilter := parseDate(params)
        filters = append(filters, dateTimeFilter...)
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

		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]}")
	})

	openapiRegister(false)
    scdrRegister(false)

	cli.Root.Execute()
}
