package middleware

import (
	"github.com/cedardevs/onestop/cli/internal/pkg/parse"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strings"
)

//this function is pre-request RegisterBefore
func ParseOneStopRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {
	filters := []string{}
	queries := []string{}

	dateTimeFilter := parse.ParseDate(params)
	filters = append(filters, dateTimeFilter...)
	startEndTimeFilter := parse.ParseStartAndEndTime(params)
	filters = append(filters, startEndTimeFilter...)
	geoSpatialFilter := parse.ParsePolygon(params)
	filters = append(filters, geoSpatialFilter...)
	query := parse.ParseTextQuery(params)
	queries = append(queries, query...)
	requestMeta := parse.ParseRequestMeta(params)
	if len(queries) > 0 || len(filters) > 0 {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
	}
}
