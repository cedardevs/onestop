package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strconv"
)

const scdrFileCmd = "scdr-files"

// const regexFileCmd = "re-file"
const typeDescription = "Search only for files of the specified data collection using the collection's file identfier. Using this option is highly recommended for any kind of file searches. Collection identifiers are case sensitive."
const regexDescription = "Locate files whose names match the case-insensitive regular expression REGEX. Only one regular expression is allowed, not longer than 100 characters."

const scdrExampleCommands = `scdr-files --available 5b58de08-afef-49fb-99a1-9c5d5c003bde
scdr-files --type 5b58de08-afef-49fb-99a1-9c5d5c003bde
scdr-files --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"
scdr-files --date=10/01
scdr-files --stime "March 31st 2003 at 17:30" --etime "2003-04-01 10:32:49"
`

var availableSummaryHeader = []string{"Collection | Description ", "---------- | -----------"}

func setScdrFlags() {
	//flags are in flags.go
	cli.AddFlag(scdrFileCmd, dateFilterFlag, dateFilterShortFlag, dateDescription, "")
	cli.AddFlag(scdrFileCmd, typeFlag, typeShortFlag, typeDescription, "")
	cli.AddFlag(scdrFileCmd, spatialFilterFlag, spatialFilterShortFlag, areaDescription, "")
	// cli.AddFlag(scdrFileCmd, regexFileCmd, "", regexDescription, "")
	cli.AddFlag(scdrFileCmd, textQueryFlag, textQueryShortFlag, queryDescription, "")
	cli.AddFlag(scdrFileCmd, maxFlag, maxShortFlag, maxDescription, "")
	cli.AddFlag(scdrFileCmd, offsetFlag, offsetShortFlag, offsetDescription, "")
	cli.AddFlag(scdrFileCmd, startTimeFlag, startTimeShortFlag, startTimeDescription, "")
	cli.AddFlag(scdrFileCmd, startTimeScdrFlag, "", startTimeScdrDescription, "")
	cli.AddFlag(scdrFileCmd, endTimeFlag, endTimeShortFlag, endTimeDescription, "")
	cli.AddFlag(scdrFileCmd, endTimeScdrFlag, "", endTimeScdrDescription, "")
	cli.AddFlag(scdrFileCmd, availableFlag, availableShortFlag, availableDescription, false)
	cli.AddFlag(scdrFileCmd, metadataFlag, metadataShortFlag, metadataDescription, "")

	cli.RegisterBefore(scdrFileCmd, parseScdrRequestFlags)
	cli.RegisterAfter(scdrFileCmd, func(cmd string, params *viper.Viper, resp *gentleman.Response, data interface{}) interface{} {
		scdrResp := marshalScdrResponse(params, data)
		return scdrResp
	})
}

func marshalScdrResponse(params *viper.Viper, data interface{}) interface{} {
	isSummary := params.GetString("available")

	dataMap := data.(map[string]interface{})
	responseMap := make(map[string]interface{})
	summaryResponse := availableSummaryHeader
	links := []string{}

	items := dataMap["data"].([]interface{})
	meta := dataMap["meta"].(map[string]interface{})

	if len(items) > 0 {
		for _, v := range items {
			value := v.(map[string]interface{})
			attr := value["attributes"].(map[string]interface{})

			if isSummary == "true" {
				fileId := attr["fileIdentifier"].(string)
				description := attr["title"].(string)
				row := fileId + " | " + description

				if count, ok := meta["totalGranules"].(float64); ok {
					summaryResponse[0] = summaryResponse[0] + " |  Total files"
					summaryResponse[1] = summaryResponse[1] + " |  ----------"
					countString := strconv.FormatFloat(count, 'f', 0, 64)
					row = row + " | " + countString
				}
				summaryResponse = append(summaryResponse, row)
			} else {
				itemLinks := attr["links"].([]interface{})
				for _, link := range itemLinks {
					url := link.(map[string]interface{})["linkUrl"].(string)
					links = append(links, url)
				}
			}

		}
		if isSummary == "true" {
			responseMap["summary"] = summaryResponse
		} else {
			responseMap["links"] = links
		}
	}
	return responseMap
}
