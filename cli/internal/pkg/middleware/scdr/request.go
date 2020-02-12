package middleware

import (
  "github.com/cedardevs/onestop/cli/internal/pkg/flags"
  "github.com/cedardevs/onestop/cli/internal/pkg/parse"
  "gopkg.in/h2non/gentleman.v2"
  "github.com/spf13/viper"
  "strings"
)
func ParseScdrRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {

	//apply a default filter for STAR
	filters := []string{"{\"type\":\"facet\",\"name\":\"dataCenters\",\"values\":[\"DOC/NOAA/NESDIS/STAR > Center for Satellite Applications and Research, NESDIS, NOAA, U.S. Department of Commerce\"]}"}
	queries := []string{}

	// isSummaryWithType := params.GetString(AvailableFlag) == "true" && len(params.GetString("type")) > 0

	collectionIdFilter := utils.ParseTypeFlag(params)
	filters = append(filters, collectionIdFilter...)
	// datacenterFilter := parseAvailableFlag(params)
	// filters = append(filters, datacenterFilter...)
	dateTimeFilter := utils.ParseDate(params)
	filters = append(filters, dateTimeFilter...)
	yearFilter := utils.ParseYear(params)
	filters = append(filters, yearFilter...)
	startEndTimeFilter := utils.ParseStartAndEndTime(params)
	filters = append(filters, startEndTimeFilter...)
	geoSpatialFilter := utils.ParsePolygon(params)
	filters = append(filters, geoSpatialFilter...)

	satnameQuery := utils.ParseSatName(params)
	queries = append(queries, satnameQuery...)
	fileNameQuery := utils.ParseFileName(params)
	queries = append(queries, fileNameQuery...)
	refileNameQuery := utils.ParseRegexFileName(params)
	queries = append(queries, refileNameQuery...)
	query := utils.ParseTextQuery(params)
	queries = append(queries, query...)
	keyWordFilter := utils.ParseKeyword(params)
	queries = append(queries, keyWordFilter...)
	requestMeta := utils.ParseRequestMeta(params)

	parseSort := utils.ParseSort(params)

	if len(queries) > 0 || len(filters) > 0 {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"summary\":false, \"sort\":[" + parseSort + "], \"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
	}
}


func TranslateArgs(params *viper.Viper) *viper.Viper {
	typeArg := params.GetString(flags.TypeFlag)
	err := viper.ReadInConfig()
	if err != nil {
		return params
	}
	scdrTypeIds := viper.Get("scdr-types").(map[string]interface{})
	uuid := scdrTypeIds[strings.ToLower(typeArg)]
	params.Set("type", uuid)
	return params
}

func DetermineEndpoint(params *viper.Viper, isSummaryWithType bool) string {
	endpoint := "/search/flattened-granule"

	if isSummaryWithType {
		collectionId := params.GetString(flags.TypeFlag)
		endpoint = "/collection/" + collectionId
	} else if params.GetString(flags.AvailableFlag) == "true" {
		endpoint = "/search/collection"
	}
	return endpoint
}
