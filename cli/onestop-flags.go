package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
)

const searchCollectionCmd = "searchcollection"
const searchGranuleCmd = "searchgranule"
const searchFlattenedGranuleCmd = "searchflattenedgranule"

const textQueryFlag = "q"
const dateFilterFlag = "date"
const spatialFilterFlag = "area"
const maxFlag = "max"
const offsetFlag = "offset"

const queryDescription = "Search flattened granules with text query"
const dateDescription = "DATE must be a string describing a date, e.g. 2010-01-30. Current year is assumed if the year part in DATE is ommited, e.g. 01-30. Any time information in DATE is disregarded. Files with data between DATE midnight and next day midnight are selected."
const areaDescription = "Locate files which intersect with the specified polygon AREA. The polygon must be a regular one (closed, no self-intersection, no hole) with coordinates (longitude, latitude) separated by \",\" like POLYGON((30.31 60.2, 31.21 60.2, 31.21 60.76, 30.31 60.76, 30.31 60.2)) "
const maxDescription = "Maximum number of results returned."
const offsetDescription = "Page number starting at 0"

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

	cli.AddFlag(searchCollectionCmd, offsetFlag, "", offsetDescription, "")
	cli.AddFlag(searchGranuleCmd, offsetFlag, "", offsetDescription, "")
	cli.AddFlag(searchFlattenedGranuleCmd, offsetFlag, "", offsetDescription, "")

}
