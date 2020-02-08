package onestop

import (
	"gopkg.in/h2non/gentleman.v2"
	"github.com/spf13/viper"
	"strings"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/CEDARDEVS/onestop/cli/internal/pkg/flags"
	"github.com/CEDARDEVS/onestop/cli/internal/pkg/utils"
)

const SearchCollectionCmd = "searchcollection"
const SearchGranuleCmd = "searchgranule"
const SearchFlattenedGranuleCmd = "searchflattenedgranule"

func InjectMiddleware(){
	cli.RegisterBefore(SearchCollectionCmd, ParseOneStopRequestFlags)
	cli.RegisterBefore(SearchGranuleCmd, ParseOneStopRequestFlags)
	cli.RegisterBefore(SearchFlattenedGranuleCmd, ParseOneStopRequestFlags)
}

//flags in flags.go
func SetOneStopFlags() {
	//func AddFlag(path, name, short, description string, defaultValue interface{})
	cli.AddFlag(SearchCollectionCmd, flags.TextQueryFlag, flags.TextQueryShortFlag, flags.QueryDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.TextQueryFlag, flags.TextQueryShortFlag, flags.QueryDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.TextQueryFlag, flags.TextQueryShortFlag, flags.QueryDescription, "")

	cli.AddFlag(SearchCollectionCmd, flags.DateFilterFlag, flags.DateFilterShortFlag, flags.DateDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.DateFilterFlag, flags.DateFilterShortFlag, flags.DateDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.DateFilterFlag, flags.DateFilterShortFlag, flags.DateDescription, "")

	cli.AddFlag(SearchCollectionCmd, flags.SpatialFilterFlag, flags.SpatialFilterShortFlag, flags.AreaDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.SpatialFilterFlag, flags.SpatialFilterShortFlag, flags.AreaDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.SpatialFilterFlag, flags.SpatialFilterShortFlag, flags.AreaDescription, "")

	cli.AddFlag(SearchCollectionCmd, flags.MaxFlag, flags.MaxShortFlag, flags.MaxDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.MaxFlag, flags.MaxShortFlag, flags.MaxDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.MaxFlag, flags.MaxShortFlag, flags.MaxDescription, "")

	cli.AddFlag(SearchCollectionCmd, flags.OffsetFlag, flags.OffsetShortFlag, flags.OffsetDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.OffsetFlag, flags.OffsetShortFlag, flags.OffsetDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.OffsetFlag, flags.OffsetShortFlag, flags.OffsetDescription, "")

	cli.AddFlag(SearchCollectionCmd, flags.EndTimeFlag, flags.EndTimeShortFlag, flags.EndTimeDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.EndTimeFlag, flags.EndTimeShortFlag, flags.EndTimeDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.EndTimeFlag, flags.EndTimeShortFlag, flags.EndTimeDescription, "")

	cli.AddFlag(SearchCollectionCmd, flags.StartTimeFlag, flags.StartTimeShortFlag, flags.StartTimeScdrDescription, "")
	cli.AddFlag(SearchGranuleCmd, flags.StartTimeFlag, flags.StartTimeShortFlag, flags.StartTimeScdrDescription, "")
	cli.AddFlag(SearchFlattenedGranuleCmd, flags.StartTimeFlag, flags.StartTimeShortFlag, flags.StartTimeScdrDescription, "")
}

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
