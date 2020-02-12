package middleware

import(
  "github.com/cedardevs/onestop/cli/internal/pkg/utils"
  "gopkg.in/h2non/gentleman.v2"
  "github.com/spf13/viper"
  "strings"
)

//this function is pre-request RegisterBefore
func ParseOneStopRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {
	filters := []string{}
	queries := []string{}

	dateTimeFilter := utils.ParseDate(params)
	filters = append(filters, dateTimeFilter...)
	startEndTimeFilter := utils.ParseStartAndEndTime(params)
	filters = append(filters, startEndTimeFilter...)
	geoSpatialFilter := utils.ParsePolygon(params)
	filters = append(filters, geoSpatialFilter...)
	query := utils.ParseTextQuery(params)
	queries = append(queries, query...)
	requestMeta := utils.ParseRequestMeta(params)
	if len(queries) > 0 || len(filters) > 0 {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
	}
}
