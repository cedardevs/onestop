package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
)

func setOneStopFlags() {
	//func AddFlag(path, name, short, description string, defaultValue interface{})
  cli.AddFlag(searchCollectionCmd, textQueryFlag, "", queryDescription, "")
  cli.AddFlag(searchGranuleCmd, textQueryFlag, "", queryDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, textQueryFlag, "", queryDescription, "")

  cli.AddFlag(searchCollectionCmd, dateFilterFlag, "", dateDescription, "")
  cli.AddFlag(searchGranuleCmd, dateFilterFlag, "", dateDescription, "")
  cli.AddFlag(searchFlattenedGranuleCmd, dateFilterFlag, "", dateDescription, "")

  cli.AddFlag(searchCollectionCmd, spatialFilterFlag, "", areaDescription, "")
  cli.AddFlag(searchGranuleCmd, spatialFilterFlag, "", areaDescription, "")
  cli.AddFlag(searchFlattenedGranuleCmd, spatialFilterFlag, "", areaDescription, "")

  cli.RegisterBefore(searchCollectionCmd, parseOneStopRequestFlags)
  cli.RegisterBefore(searchGranuleCmd, parseOneStopRequestFlags)
  cli.RegisterBefore(searchFlattenedGranuleCmd, parseOneStopRequestFlags)

	cli.AddFlag(searchCollectionCmd, maxFlag, "", maxDescription, "")
	cli.AddFlag(searchGranuleCmd, maxFlag, "", maxDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, maxFlag, "", maxDescription, "")

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
