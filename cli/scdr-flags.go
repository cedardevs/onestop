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

const scdrExampleCommands = `scdr-files --available -t 5b58de08-afef-49fb-99a1-9c5d5c003bde
scdr-files --type 5b58de08-afef-49fb-99a1-9c5d5c003bde
scdr-files --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"
scdr-files --date=10/01
scdr-files --stime "March 31st 2003 at 17:30" --etime "2003-04-01 10:32:49"
`

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
	responseMap := data.(map[string]interface{})
	translatedResponseMap := transformResponse(params, responseMap)
	return translatedResponseMap
}

func transformResponse(params *viper.Viper, responseMap map[string]interface{}) map[string]interface{} {
	translatedResponseMap := make(map[string]interface{})

	if items, ok := responseMap["data"].([]interface{}); ok {
		isSummary := params.GetString("available")
		if isSummary == "true" {
			count := getCount(responseMap)
			translatedResponseMap["summary"] = buildSummary(items, count)
		} else {
			translatedResponseMap["links"] = buildLinkResponse(items)
		}
	}

	return translatedResponseMap
}

func getCount(responseMap map[string]interface{}) string {
	meta := responseMap["meta"].(map[string]interface{})
	count := ""
	if totalGranules, ok := meta["totalGranules"].(float64); ok {
		count = strconv.FormatFloat(totalGranules, 'f', 0, 64)
	}
	return count
}

func buildSummary(items []interface{}, count string) []string {

	summaryResponse := buildSummaryHeaders(items, count)

	if len(count) > 0 {
		count = count + " | "
		countColumnWidth := 15 - len(count)
		for i := 0; i < countColumnWidth; i++ {
			count = " " + count
		}
	}

	for _, v := range items {
		value := v.(map[string]interface{})
		attr := value["attributes"].(map[string]interface{})

		id := value["id"].(string) + " | "
		fileId := attr["fileIdentifier"].(string) + " | "

		description := attr["title"].(string)
		row := id + fileId + count + description

		summaryResponse = append(summaryResponse, row)
	}

	return summaryResponse
}

func buildLinkResponse(items []interface{}) []string {
	links := []string{}
	for _, v := range items {
		value := v.(map[string]interface{})
		attr := value["attributes"].(map[string]interface{})
		itemLinks := attr["links"].([]interface{})
		for _, link := range itemLinks {
			url := link.(map[string]interface{})["linkUrl"].(string)
			links = append(links, url)
		}
	}
	return links
}

func buildSummaryHeaders(items []interface{}, count string) []string {
	uuidHeader, uuidSubHeader := buildIdHeaders(items)

	fileIdentifierHeader, fileIdentifierSubHeader := buildFileIdHeaders(items)
	descriptionHeader, descriptionSubHeader := buildDescriptionHeaders(items)
	countHeader, countSubHeader := buildCountHeader(count)

	summaryResponse := []string{
		uuidHeader + fileIdentifierHeader + countHeader + descriptionHeader,
		uuidSubHeader + fileIdentifierSubHeader + countSubHeader + descriptionSubHeader,
	}

	return summaryResponse
}

func buildIdHeaders(items []interface{}) (string, string) {
	uuidHeader := "OneStop ID "
	uuidSubHeader := "-----------"
	//calculate subheader length based on the first IDs length
	idLength := len(items[0].(map[string]interface{})["id"].(string))
	uuidHeader, uuidSubHeader = formatHeader(uuidHeader, uuidSubHeader, idLength)
	return uuidHeader, uuidSubHeader
}

func buildFileIdHeaders(items []interface{}) (string, string) {
	fileIdentifierHeader := "FileIdentifier"
	fileIdentifierSubHeader := "--------------"
	fileIdentifierLength := len(items[0].(map[string]interface{})["attributes"].(map[string]interface{})["fileIdentifier"].(string))
	fileIdentifierHeader, fileIdentifierSubHeader = formatHeader(fileIdentifierHeader, fileIdentifierSubHeader, fileIdentifierLength)

	return fileIdentifierHeader, fileIdentifierSubHeader
}

func buildDescriptionHeaders(items []interface{}) (string, string) {
	descriptionHeader := "Description"
	descriptionSubHeader := "-----------"
	descriptionLength := len(items[0].(map[string]interface{})["attributes"].(map[string]interface{})["title"].(string))
	descriptionHeader, descriptionSubHeader = formatHeader(descriptionHeader, descriptionSubHeader, descriptionLength)

	return descriptionHeader, descriptionSubHeader
}

func buildCountHeader(count string) (string, string) {
	countHeader := ""
	countSubHeader := ""
	countLength := len(count)

	//count does not appear in a summary view without --type
	if countLength > 0 {
		countHeader = "Total files "
		countSubHeader = "------------"
		countHeader, countSubHeader = formatHeader(countHeader, countSubHeader, countLength)
	}

	return countHeader, countSubHeader
}

func formatHeader(header string, subHeader string, columnWidth int) (string, string) {
	headerLength := len(header)
	for i := 0; i <= columnWidth; i++ {
		if i == columnWidth {
			header = header + " | "
			subHeader = subHeader + " | "
		} else {
			if i >= headerLength {
				header = header + " "
				subHeader = "-" + subHeader
			}
		}
	}
	return header, subHeader
}