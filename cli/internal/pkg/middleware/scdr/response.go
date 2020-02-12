package middleware

import(
  "github.com/spf13/viper"
  "strconv"
  "time"
  "github.com/cedardevs/onestop/cli/internal/pkg/flags"
  "fmt"
)

func MarshalScdrResponse(params *viper.Viper, data interface{}) interface{} {
	responseMap := data.(map[string]interface{})
	translatedResponseMap := transformResponse(params, responseMap)
	return translatedResponseMap
}

func transformResponse(params *viper.Viper, responseMap map[string]interface{}) map[string]interface{} {
	translatedResponseMap := make(map[string]interface{})
	scdrOuput := []string{}

	if items, ok := responseMap["data"].([]interface{}); ok && len(items) > 0 {
		isSummary := params.GetString(flags.AvailableFlag)
		gapInterval := params.GetString(flags.GapFlag)
    typeArg := params.GetString(flags.TypeFlag)
    fmt.Println(typeArg)
    fmt.Println(isSummary)

    //gap is ignored if no type is passed or if --available is passed
		if len(gapInterval) > 0 && len(typeArg) > 0 && isSummary ==  "false" {
			scdrOuput = FindGaps(gapInterval, items)
		} else if isSummary == "true" {
			count := getCount(responseMap)
			scdrOuput = buildSummary(items, count)
		} else {
			scdrOuput = buildLinkResponse(items)
		}
		translatedResponseMap["scdr-output"] = scdrOuput
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

func FindGaps(gapInterval string, items []interface{}) []string{
	gapResponse := []string{
		"Gap Start Time           | Gap End Time             | Gap Duration",
		"-------------------------+--------------------------+-------------",
	}
	dateFormat := "2006-01-02T15:04:05.000Z"
	interval, _  := time.ParseDuration(gapInterval)
	if len(items) == 0 {
		return  gapResponse
	}
	firstItem := items[0].(map[string]interface{})
	firstItemAttrs := firstItem["attributes"].(map[string]interface{})
	lastEndDate, _ := time.Parse(dateFormat, firstItemAttrs["endDate"].(string))

	for _, v := range items{
		 item := v.(map[string]interface{})
		 attrs := item["attributes"].(map[string]interface{})
     start, _ := time.Parse(dateFormat, attrs["beginDate"].(string))
		 end, _ := time.Parse(dateFormat, attrs["endDate"].(string))
		 //if the temporal distance from this file and the last is greater than interval
		 //add it to new output
		 if start.Sub(lastEndDate) > interval {
       gapString := lastEndDate.Format("2006-01-02T15:04:05.000Z") + " | " + start.Format("2006-01-02T15:04:05.000Z") + " | " + start.Sub(lastEndDate).String()
			 gapResponse = append(gapResponse, gapString)
		 }
		 lastEndDate = end
	}

	return gapResponse
}

func buildSummary(items []interface{}, count string) []string {

	summaryResponse := buildSummaryHeaders(items, count)

	if len(count) > 0 {
		count = count + " | "
		//wip- 15 is hardcoded approx length of count headers
		countColumnWidth := 15 - len(count)
		for i := 0; i < countColumnWidth; i++ {
			count = " " + count
		}
	}
	for _, v := range items {
		value := v.(map[string]interface{})
		attr := value["attributes"].(map[string]interface{})
		id := value["id"].(string)
		err := viper.ReadInConfig()
		if err == nil {
			id = reverseLookup(id)
		}
		id = id + " | "
		fileId := attr["fileIdentifier"].(string) + " | "
		description := attr["title"].(string)
		row := id + fileId + count + description

		summaryResponse = append(summaryResponse, row)
	}

	return summaryResponse
}

func reverseLookup(id string) string {
	scdrTypeIds := viper.Get("scdr-types").(map[string]interface{})
	for k, v := range scdrTypeIds {
		if id == v {
			return k
		}
	}
	return id
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
	id := items[0].(map[string]interface{})["id"].(string)
	err := viper.ReadInConfig()
	if err == nil {
		id = reverseLookup(id)
	}
	idLength := len(id)
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
	if descriptionLength > 100 {
		descriptionLength = 100
	}
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
