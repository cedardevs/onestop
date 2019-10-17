package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strings"
)

const scdrFileCmd = "scdr-files"
const typeFlag = "type"
const regexFileCmd = "re-file"
const typeDescription = "Search only for files of the specified data collection using the collection's file identfier. Using this option is highly recommended for any kind of file searches. Collection identifiers are case sensitive."
const regexDescription = "Locate files whose names match the case-insensitive regular expression REGEX. Only one regular expression is allowed, not longer than 100 characters."

func setScdrFlags(){
	cli.AddFlag(scdrFileCmd, dateFilterFlag, "", dateDescription, "")
	cli.AddFlag(scdrFileCmd, typeFlag, "", typeDescription, "")
	cli.AddFlag(scdrFileCmd, spatialFilterFlag, "", areaDescription, "")
	cli.AddFlag(scdrFileCmd, regexFileCmd, "", regexDescription, "")
	cli.AddFlag(scdrFileCmd, textQueryFlag, "", queryDescription, "")

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

		if len(queries) > 0 || len(filters) > 0  {
	    req.AddHeader("content-type", "application/json")
	    req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]}")
	  }
	})
}
