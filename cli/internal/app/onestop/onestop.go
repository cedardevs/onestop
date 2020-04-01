package onestop

import (
	"github.com/cedardevs/onestop/cli/internal/pkg/flags"
	"github.com/cedardevs/onestop/cli/internal/pkg/middleware/onestop"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
)

const SearchCollectionCmd = "searchcollection"
const SearchGranuleCmd = "searchgranule"
const SearchFlattenedGranuleCmd = "searchflattenedgranule"

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

func InjectMiddleware() {
	cli.RegisterBefore(SearchCollectionCmd, middleware.ParseOneStopRequestFlags)
	cli.RegisterBefore(SearchGranuleCmd, middleware.ParseOneStopRequestFlags)
	cli.RegisterBefore(SearchFlattenedGranuleCmd, middleware.ParseOneStopRequestFlags)
}
