package scdr

import (
	"fmt"
	"github.com/cedardevs/onestop/cli/internal/pkg/flags"
	"github.com/cedardevs/onestop/cli/internal/pkg/middleware/scdr"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/pkg/errors"
	"github.com/rs/zerolog/log"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	_ "math"
	"strconv"
	"strings"
	"time"
)

const ScdrFileCmd = "scdr-files"

const scdrExampleCommands = `scdr-files --available -t ABI-L1b-Rad --cloud
scdr-files --type 5b58de08-afef-49fb-99a1-9c5d5c003bde
scdr-files --area "POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"
scdr-files --date 10/01
scdr-files --stime "March 31st 2003 at 17:30" --etime "2003-04-01 10:32:49"
`

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
	cli.AddFlag(ScdrFileCmd, flags.SinceFlag, flags.SinceShortFlag, flags.SinceDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.MonthFlag, flags.MonthShortFlag, flags.MonthDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.DayFlag, flags.DayShortFlag, flags.DayDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.DoyFlag, flags.DoyShortFlag, flags.DoyDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.SearchAfterFlag, flags.SearchAfterShortFlag, flags.SearchAfterDescription, "")

	//not scdr-files specific
	cli.AddFlag(ScdrFileCmd, flags.MaxFlag, flags.MaxShortFlag, flags.MaxDescription, flags.MaxDefault)
	cli.AddFlag(ScdrFileCmd, flags.TextQueryFlag, flags.TextQueryShortFlag, flags.QueryDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.CloudServerFlag, flags.CloudServerShortFlag, flags.CloudServerDescription, false)
	cli.AddFlag(ScdrFileCmd, flags.TestServerFlag, flags.TestServerShortFlag, flags.TestServerDescription, false)
	// 	cli.AddFlag(ScdrFileCmd, flags.SortFlag, flags.SortShortFlag, flags.SortDescription, flags.SortDefault)
	cli.AddFlag(ScdrFileCmd, flags.ChecksumFlag, flags.ChecksumShortFlag, flags.ChecksumDescription, "")

}

func InjectMiddleware() {
	cli.RegisterBefore(ScdrFileCmd, middleware.ParseScdrRequestFlags)
	cli.RegisterAfter(ScdrFileCmd, func(cmd string, params *viper.Viper, resp *gentleman.Response, data interface{}) interface{} {
		scdrResp := middleware.MarshalScdrResponse(params, data)
		return scdrResp
	})
}

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

				resultList := makeRequests(params, body)

				if len(resultList) > 0 {
					for _, row := range resultList {
						fmt.Println(strings.TrimSpace(row))
					}
				}

			},
		}
		root.AddCommand(cmd)

		cli.SetCustomFlags(cmd)

		if cmd.Flags().HasFlags() {
			params.BindPFlags(cmd.Flags())
		}

	}()
}

func makeRequests(params *viper.Viper, body string) []string {
	var results []string
	var aggregateItems []interface{}
	handlerPath := "scdr-files"
	isSummary := params.GetString(flags.AvailableFlag)

	// the maximum number of results to fetch
	max, _ := strconv.Atoi(params.GetString(flags.MaxFlag))
	maxPageSize := 1000     //search API limit
	pageSize := maxPageSize //might as default to max

	//only get what they want if one request does it
	if max < maxPageSize {
		pageSize = max
	}

	//only get one page in the event this is a summary view
	pagesNeeded := 1

	//the map and resp that will pass to HandleAfter (middleware.MarshalScdrResponse) when all is done
	var decoded map[string]interface{}
	var resp *gentleman.Response

	for i := 1; i <= pagesNeeded; i++ {
		//choose endpoint
		req := buildRequest(params, body)

		//trigger middleware - uses params to update request body
		cli.HandleBefore(handlerPath, params, req)

		//do the request
		resp, err := req.Do()

		if err != nil {
			fmt.Println("Request failed")
			log.Fatal().Err(errors.Wrap(err, "Request failed")).Msg("Error calling operation")
		}

		if resp.StatusCode < 400 {
			if err := cli.UnmarshalResponse(resp, &decoded); err != nil {
				fmt.Println("Unmarshalling response failed")
				log.Fatal().Err(errors.Wrap(errors.Wrap(err, "Unmarshalling response failed"), "Request failed")).Msg("Error calling operation")
			}
		} else {
			fmt.Println("FAIL greater than 400")
			log.Fatal().Err(errors.Errorf("HTTP %d: %s", resp.StatusCode, resp.String())).Msg("Error calling operation")
		}

		//update the pages needed based on the number of results
		if meta, ok := decoded["meta"].(map[string]interface{}); ok && isSummary == "false" {
			resultCount := int(meta["total"].(float64))
			remainder := 0
			if resultCount <= 0 {
				log.Fatal().Msg("No results")
				return results
			}
			if resultCount < max {
				pagesNeeded = int(resultCount/pageSize) + 1
				remainder = resultCount - (pageSize * i)
			} else {
				//default to max requests
				pagesNeeded = int(max / pageSize)
				remainder = max - (pageSize * i)
			}

			if remainder > 0 && remainder < pageSize {
				params.Set(flags.MaxFlag, remainder)
			}
		}

		//aggregate items from each call
		if items, ok := decoded["data"].([]interface{}); ok {
			aggregateItems = append(aggregateItems, items...)
			//we dont want many pages if we are getting a summary
			if len(items) > 0 && isSummary == "false" {
				lastItem := items[len(items)-1].(map[string]interface{})
				lastItemAttrs := lastItem["attributes"].(map[string]interface{})
				lastItemBeginDate := lastItemAttrs["beginDate"].(string)
				lastItemStagedDate := lastItemAttrs["stagedDate"].(float64)

				if len(lastItemBeginDate) > 0 {
					nextAfterBeginDate, err := time.Parse("2006-01-02T15:04:05Z", lastItemBeginDate)
					if err != nil {
						fmt.Println("Cannot parse begin date")
					}
					nextAfterBeginEpoch := nextAfterBeginDate.UnixNano() / 1000000

					if err != nil {
						fmt.Println("Cannot parse staged date")
					}
					searchAfterBeginDate := strconv.FormatInt(nextAfterBeginEpoch, 10)
					searchAfterStagedDate := strconv.FormatInt(int64(lastItemStagedDate), 10)
					searchAfter := searchAfterBeginDate + ", " + searchAfterStagedDate
					params.Set(flags.SearchAfterFlag, searchAfter)
				}
			}
		}
	}
	//make it like on big response so response middleware knows how to parse it
	decoded["data"] = aggregateItems
	//trigger middleware response - build scdr-files style output
	after := cli.HandleAfter(handlerPath, params, resp, decoded)
	if after != nil {
		decoded = after.(map[string]interface{})
	}
	if output, ok := decoded["scdr-output"].([]string); ok {
		results = output
	}
	return results
}

func buildRequest(params *viper.Viper, body string) *gentleman.Request {
	server := viper.GetString("server")
	if params.GetString("test") == "true" {
		viper.Set("server-index", 1)
	} else if params.GetString(flags.CloudFlag) == "true" {
		viper.Set("server-index", 2)
	}

	if server == "" {
		server = scdrServers()[viper.GetInt("server-index")]["url"]
	}

	//the summary view with a type includes a count
	//without the type (parentId) we cannot get count
	isSummaryWithType := params.GetString(flags.AvailableFlag) == "true" && len(params.GetString(flags.TypeFlag)) > 0

	/// default endpoint: search/flattened-granule
	// /collection/{id} if we have type to get count
	endpoint := middleware.DetermineEndpoint(params, isSummaryWithType)

	url := server + endpoint

	var req *gentleman.Request

	if isSummaryWithType {
		req = cli.Client.Get().URL(url)
	} else {
		req = cli.Client.Post().URL(url)
	}

	if body != "" {
		req = req.AddHeader("Content-Type", "application/json").BodyString(body)
	}

	return req
}

func scdrServers() []map[string]string {
	return []map[string]string{

		map[string]string{
			"description": "NOAA OneStop",
			"url":         "https://data.noaa.gov/onestop-search",
		},

		map[string]string{
			"description": "Development test server (uses test data)",
			"url":         "https://sciapps.colorado.edu/onestop-search",
		},

		map[string]string{
			"description": "Development cloud server (uses test data)",
			"url":         "http://9bcc428b-default-osclient-d008-199379672.us-east-1.elb.amazonaws.com/onestop-search",
		},
	}
}
