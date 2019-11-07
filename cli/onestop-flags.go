package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
)

func setOneStopFlags() {
	//func AddFlag(path, name, short, description string, defaultValue interface{})
  cli.AddFlag(searchCollectionCmd, textQueryFlag, textQueryShortFlag, queryDescription, "")
  cli.AddFlag(searchGranuleCmd, textQueryFlag, textQueryShortFlag, queryDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, textQueryFlag, textQueryShortFlag, queryDescription, "")

  cli.AddFlag(searchCollectionCmd, dateFilterFlag, dateFilterShortFlag, dateDescription, "")
  cli.AddFlag(searchGranuleCmd, dateFilterFlag, dateFilterShortFlag, dateDescription, "")
  cli.AddFlag(searchFlattenedGranuleCmd, dateFilterFlag, dateFilterShortFlag, dateDescription, "")

  cli.AddFlag(searchCollectionCmd, spatialFilterFlag, spatialFilterShortFlag, areaDescription, "")
  cli.AddFlag(searchGranuleCmd, spatialFilterFlag, spatialFilterShortFlag, areaDescription, "")
  cli.AddFlag(searchFlattenedGranuleCmd, spatialFilterFlag, spatialFilterShortFlag, areaDescription, "")

  cli.RegisterBefore(searchCollectionCmd, parseOneStopRequestFlags)
  cli.RegisterBefore(searchGranuleCmd, parseOneStopRequestFlags)
  cli.RegisterBefore(searchFlattenedGranuleCmd, parseOneStopRequestFlags)

	cli.AddFlag(searchCollectionCmd, maxFlag, maxShortFlag, maxDescription, "")
	cli.AddFlag(searchGranuleCmd, maxFlag, maxShortFlag, maxDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, maxFlag, maxShortFlag, maxDescription, "")

	cli.AddFlag(searchCollectionCmd, offsetFlag, offsetShortFlag, offsetDescription, "")
	cli.AddFlag(searchGranuleCmd, offsetFlag, offsetShortFlag, offsetDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, offsetFlag, offsetShortFlag, offsetDescription, "")

	cli.AddFlag(searchCollectionCmd, endTimeFlag, endTimeShortFlag, endTimeDescription, "")
	cli.AddFlag(searchGranuleCmd, endTimeFlag, endTimeShortFlag, endTimeDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, endTimeFlag, endTimeShortFlag, endTimeDescription, "")

	cli.AddFlag(searchCollectionCmd, startTimeFlag, startTimeShortFlag, startTimeScdrDescription, "")
	cli.AddFlag(searchGranuleCmd, startTimeFlag, startTimeShortFlag, startTimeScdrDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, startTimeFlag, startTimeShortFlag, startTimeScdrDescription, "")


}
