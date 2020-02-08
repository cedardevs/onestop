package scdr

import (
	"fmt"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/pkg/errors"
	"github.com/rs/zerolog/log"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strings"
	"github.com/CEDARDEVS/onestop/cli/internal/pkg/flags"
	"github.com/CEDARDEVS/onestop/cli/internal/app/generated"
	"github.com/CEDARDEVS/onestop/cli/internal/pkg/utils"
	"github.com/CEDARDEVS/onestop/cli/internal/pkg/middleware"
)

const ScdrFileCmd = "scdr-files"

const scdrExampleCommands = `scdr-files --available -t ABI-L1b-Rad --cloud
scdr-files --type 5b58de08-afef-49fb-99a1-9c5d5c003bde
scdr-files --area "POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"
scdr-files --date 10/01
scdr-files --stime "March 31st 2003 at 17:30" --etime "2003-04-01 10:32:49"
`

func ScdrRegister() {
	root := cli.Root
	cli.Root.Short = "SCDR OneStop Search API"
	cli.Root.Long = cli.Markdown("Search Collections and Granules! More information on search request and responses available at [Search API Requests](https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Requests) and [Search API Responses](https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Responses).")

	//support for scdr-files type
	viper.SetConfigName("scdr-files-config")
	viper.AddConfigPath("/etc/scdr-files/")
	viper.AddConfigPath("$HOME/.scdr-files")
	viper.AddConfigPath(".")
	//this is for the container
	viper.AddConfigPath("/")

	func() {
		params := viper.New()

		//scdrExampleCommands ini scdr_flags.go
		var examples string = scdrExampleCommands

		cmd := &cobra.Command{
			Use:     "scdr-files",
			Short:   "An SCDR interface for OneStop",
			Long:    cli.Markdown("Supports SCDR syntax for searching OneStop. See flags for currently supported features."),
			Example: examples,
			Args:    cobra.MinimumNArgs(0),
			Run: func(cmd *cobra.Command, args []string) {
				for _, arg := range args {
					log.Info().Msg(arg)
				}
				body, err := cli.GetBody("application/json", args[0:])
				if err != nil {
					log.Fatal().Err(err).Msg("Unable to get body")
				}

				_, decoded, err := ScdrSearch(params, body)
				if err != nil {
					log.Fatal().Err(err).Msg("Error calling operation")
				}
				scdrOutputFormatAndPrint(params, decoded)

			},
		}
		root.AddCommand(cmd)

		cli.SetCustomFlags(cmd)

		if cmd.Flags().HasFlags() {
			params.BindPFlags(cmd.Flags())
		}

	}()
}

func ScdrSearch(params *viper.Viper, body string) (*gentleman.Response, map[string]interface{}, error) {
	handlerPath := "scdr-files"

	params = translateArgs(params)
	req := buildRequest(params)

	if body != "" {
		req = req.AddHeader("Content-Type", "application/json").BodyString(body)
	}

	cli.HandleBefore(handlerPath, params, req)

	resp, err := req.Do()
	if err != nil {
		return nil, nil, errors.Wrap(err, "Request failed")
	}

	var decoded map[string]interface{}

	if resp.StatusCode < 400 {
		if err := cli.UnmarshalResponse(resp, &decoded); err != nil {
			return nil, nil, errors.Wrap(err, "Unmarshalling response failed")
		}
	} else {
		return nil, nil, errors.Errorf("HTTP %d: %s", resp.StatusCode, resp.String())
	}

	after := cli.HandleAfter(handlerPath, params, resp, decoded)
	if after != nil {
		decoded = after.(map[string]interface{})
	}

	return resp, decoded, nil
}

func translateArgs(params *viper.Viper) *viper.Viper {
	typeArg := params.GetString(flags.TypeFlag)
	err := viper.ReadInConfig()
	if err != nil {
		return params
	}
	scdrTypeIds := viper.Get("scdr-types").(map[string]interface{})
	uuid := scdrTypeIds[strings.ToLower(typeArg)]
	params.Set("type", uuid)
	return params
}

func buildRequest(params *viper.Viper) *gentleman.Request {
	server := viper.GetString("server")
	if params.GetString("test") == "true" {
		viper.Set("server-index", 1)
	}
	//since we dont have the aws instance in the openapi spec.
	if params.GetString(flags.CloudFlag) == "true" {
		server = flags.CloudUrl
	}
	if server == "" {
		server = generated.OpenapiServers()[viper.GetInt("server-index")]["url"]
	}

  //the summary view with a type includes a count
	//without the type (parentId) we cannot get count
	isSummaryWithType := params.GetString(flags.AvailableFlag) == "true" && len(params.GetString(flags.TypeFlag)) > 0

  /// default endpoint: search/flattened-granule
	// /collection/{id} if we have type to get count
	endpoint := determineEndpoint(params, isSummaryWithType)

	url := server + endpoint

	var req *gentleman.Request

	if isSummaryWithType {
		req = cli.Client.Get().URL(url)
	} else {
		req = cli.Client.Post().URL(url)
	}

	return req
}

func determineEndpoint(params *viper.Viper, isSummaryWithType bool) string {
	endpoint := "/search/flattened-granule"

	if isSummaryWithType {
		collectionId := params.GetString(flags.TypeFlag)
		endpoint = "/collection/" + collectionId
	} else if params.GetString(flags.AvailableFlag) == "true" {
		endpoint = "/search/collection"
	}
	return endpoint
}

func scdrOutputFormatAndPrint(params *viper.Viper, decoded map[string]interface{}) {
	if output, ok := decoded["scdr-ouput"].([]string); ok {
		for _, row := range output {
			fmt.Println(strings.TrimSpace(row))
		}
	} else {
		fmt.Println("No results")
	}
}

func InjectMiddleware(){
	//ParseScdrRequestFlags in parsing-util.go
	cli.RegisterBefore(ScdrFileCmd, ParseScdrRequestFlags)
	cli.RegisterAfter(ScdrFileCmd, func(cmd string, params *viper.Viper, resp *gentleman.Response, data interface{}) interface{} {
		scdrResp := middleware.MarshalScdrResponse(params, data)
		return scdrResp
	})
}

func SetScdrFlags() {
	//flags are in flags.go
	cli.AddFlag(ScdrFileCmd, flags.DateFilterFlag, flags.DateFilterShortFlag, flags.DateDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.TypeFlag, flags.TypeShortFlag, flags.TypeDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.SpatialFilterFlag, flags.SpatialFilterShortFlag, flags.AreaDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.StartTimeFlag, flags.StartTimeShortFlag, flags.StartTimeDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.StartTimeScdrFlag, "", flags.StartTimeScdrDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.EndTimeFlag, flags.EndTimeShortFlag, flags.EndTimeDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.EndTimeScdrFlag, "", flags.EndTimeScdrDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.AvailableFlag, flags.AvailableShortFlag, flags.AvailableDescription, false)
	cli.AddFlag(ScdrFileCmd, flags.MetadataFlag, flags.MetadataShortFlag, flags.MetadataDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.FileFlag, flags.FileShortFlag, flags.FileFlagDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.ReFileFlag, flags.ReFileShortFlag, flags.RegexDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.SatnameFlag, "", flags.SatnameDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.YearFlag, flags.YearShortFlag, flags.YearDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.KeywordFlag, flags.KeywordShortFlag, flags.KeywordDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.GapFlag, flags.GapShortFlag, flags.GapDescription, "")

//not scdr-files specific
	cli.AddFlag(ScdrFileCmd, flags.MaxFlag, flags.MaxShortFlag, flags.MaxDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.OffsetFlag, flags.OffsetShortFlag, flags.OffsetDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.TextQueryFlag, flags.TextQueryShortFlag, flags.QueryDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.CloudServerFlag, flags.CloudServerShortFlag, flags.CloudServerDescription, false)
	cli.AddFlag(ScdrFileCmd, flags.TestServerFlag, flags.TestServerShortFlag, flags.TestServerDescription, false)
	cli.AddFlag(ScdrFileCmd, flags.SortFlag, flags.SortShortFlag, flags.SortDescription, "")

}

func ParseScdrRequestFlags(cmd string, params *viper.Viper, req *gentleman.Request) {

	//apply a default filter for STAR
	filters := []string{"{\"type\":\"facet\",\"name\":\"dataCenters\",\"values\":[\"DOC/NOAA/NESDIS/STAR > Center for Satellite Applications and Research, NESDIS, NOAA, U.S. Department of Commerce\"]}"}
	queries := []string{}

	// isSummaryWithType := params.GetString(AvailableFlag) == "true" && len(params.GetString("type")) > 0

	collectionIdFilter := utils.ParseTypeFlag(params)
	filters = append(filters, collectionIdFilter...)
	// datacenterFilter := parseAvailableFlag(params)
	// filters = append(filters, datacenterFilter...)
	dateTimeFilter := utils.ParseDate(params)
	filters = append(filters, dateTimeFilter...)
	yearFilter := utils.ParseYear(params)
	filters = append(filters, yearFilter...)
	startEndTimeFilter := utils.ParseStartAndEndTime(params)
	filters = append(filters, startEndTimeFilter...)
	geoSpatialFilter := utils.ParsePolygon(params)
	filters = append(filters, geoSpatialFilter...)

	satnameQuery := utils.ParseSatName(params)
	queries = append(queries, satnameQuery...)
	fileNameQuery := utils.ParseFileName(params)
	queries = append(queries, fileNameQuery...)
	refileNameQuery := utils.ParseRegexFileName(params)
	queries = append(queries, refileNameQuery...)
	query := utils.ParseTextQuery(params)
	queries = append(queries, query...)
	keyWordFilter := utils.ParseKeyword(params)
	queries = append(queries, keyWordFilter...)
	requestMeta := utils.ParseRequestMeta(params)

	parseSort := utils.ParseSort(params)

	if len(queries) > 0 || len(filters) > 0 {
		req.AddHeader("content-type", "application/json")
		req.BodyString("{\"summary\":false, \"sort\":[" + parseSort + "], \"filters\":[" + strings.Join(filters, ", ") + "], \"queries\":[" + strings.Join(queries, ", ") + "]," + requestMeta + "}")
	}
}
