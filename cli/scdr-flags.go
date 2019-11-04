package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
)

const scdrFileCmd = "scdr-files"
const typeFlag = "type"
// const regexFileCmd = "re-file"
const typeDescription = "Search only for files of the specified data collection using the collection's file identfier. Using this option is highly recommended for any kind of file searches. Collection identifiers are case sensitive."
const regexDescription = "Locate files whose names match the case-insensitive regular expression REGEX. Only one regular expression is allowed, not longer than 100 characters."

func setScdrFlags(){
	//flags are in onestop-flags.go
	cli.AddFlag(scdrFileCmd, dateFilterFlag, "", dateDescription, "")
	cli.AddFlag(scdrFileCmd, typeFlag, "", typeDescription, "")
	cli.AddFlag(scdrFileCmd, spatialFilterFlag, "", areaDescription, "")
	// cli.AddFlag(scdrFileCmd, regexFileCmd, "", regexDescription, "")
	cli.AddFlag(scdrFileCmd, textQueryFlag, "", queryDescription, "")
	cli.AddFlag(scdrFileCmd, maxFlag, "", maxDescription, "")
	cli.AddFlag(scdrFileCmd, offsetFlag, "", offsetDescription, "")

	cli.RegisterBefore("scdr-files", parseScdrRequestFlags)
	cli.RegisterAfter("scdr-files", func(cmd string, params *viper.Viper, resp *gentleman.Response, data interface{}) interface{} {
		scdrResp := marshalScdrResponse(data)
		return scdrResp
	})
}


func marshalScdrResponse(data interface{}) interface{} {
	responseMap := make(map[string]interface{})
	links := []string{}
	m := data.(map[string]interface{})
	items := m["data"].([]interface {})
	if len(items) > 0 {
		for _, v := range items {
			value := v.(map[string]interface{})
			attr := value["attributes"].(map[string]interface{})
			itemLinks := attr["links"].([]interface{})
			for _, link := range itemLinks {
				url := link.(map[string]interface{})["linkUrl"].(string)
				links = append(links, url)
			}
		}
		responseMap["links"] = links
	}
	return responseMap
}
