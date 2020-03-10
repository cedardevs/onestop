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
	"strings"
	"strconv"
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
	cli.AddFlag(ScdrFileCmd, flags.OffsetFlag, flags.OffsetShortFlag, flags.OffsetDescription, flags.OffsetDefault)
	cli.AddFlag(ScdrFileCmd, flags.TextQueryFlag, flags.TextQueryShortFlag, flags.QueryDescription, "")
	cli.AddFlag(ScdrFileCmd, flags.CloudServerFlag, flags.CloudServerShortFlag, flags.CloudServerDescription, false)
	cli.AddFlag(ScdrFileCmd, flags.TestServerFlag, flags.TestServerShortFlag, flags.TestServerDescription, false)
	cli.AddFlag(ScdrFileCmd, flags.SortFlag, flags.SortShortFlag, flags.SortDescription, flags.SortDefault)
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

				_, decoded, err := ScdrSearch(params, body)
				if err != nil {
					log.Fatal().Err(err).Msg("Error calling operation")
				}
				scdrOutputFormatAndPrint(decoded)

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

	params = middleware.TranslateArgs(params)

//start paging here by modifying params
//means we parse everytime, but that is better than having to unmarshal
//the request body to modify the page args

    maximumResults := 2000
    max, _ := strconv.Atoi(params.GetString(flags.MaxFlag))
    fmt.Println("max")
    fmt.Println(max)
//     offset := params.GetString(flags.OffsetFlag)
//     searchAfter := params.GetString(flags.SearchAfterFlag)
    pagesNeeded := maximumResults / max
    var aggregateItems []interface{}
    var decoded map[string]interface{}
    var resp *gentleman.Response

    for i := 1; i <= pagesNeeded; i++ {
        fmt.Println("PAGE ", i)
      	req := buildRequest(params, body)
        cli.HandleBefore(handlerPath, params, req)
        var err error
        resp, err = req.Do()

        if err != nil {
            fmt.Println("Request failed")
            return nil, nil, errors.Wrap(err, "Request failed")
        }

        if resp.StatusCode < 400 {
            if err := cli.UnmarshalResponse(resp, &decoded); err != nil {
                fmt.Println("Unmarshalling response failed")
                return nil, nil, errors.Wrap(err, "Unmarshalling response failed")
            }
        } else {
            fmt.Println("FAIL greater than 400")
            return nil, nil, errors.Errorf("HTTP %d: %s", resp.StatusCode, resp.String())
        }

        if meta, ok := decoded["meta"].(map[string]interface{}); ok {
            resultCount := meta["total"].(float64)
            pagesNeeded = int(resultCount / float64(max)) + 1
            fmt.Println("resultCount")
            fmt.Println(resultCount)
            fmt.Println("max")
            fmt.Println(max)
            fmt.Println("pagesNeeded")
            fmt.Println(pagesNeeded)
        }

        if items, ok := decoded["data"].([]interface{}); ok {
            fmt.Println("result okay")
            fmt.Println(len(aggregateItems))
            fmt.Println(len(items))
            aggregateItems = append(aggregateItems, items...)
            fmt.Println(len(aggregateItems))

            lastItem := items[len(items)-1].(map[string]interface{})
            lastItemAttrs := lastItem["attributes"].(map[string]interface{})
            lastItemBeginDate := lastItemAttrs["beginDate"].(string)

            if len(lastItemBeginDate) > 0 {
                fmt.Println("Found lastItemBeginDate")
                nextAfterDate, err := time.Parse("2006-01-02T15:04:05Z", lastItemBeginDate)
                if err != nil {
                    fmt.Println("Cannot parse begin date")
                }
                nextAfterEpoch := nextAfterDate.UnixNano() / 1000000
//                 params.Set(flags.SearchAfterFlag, searchAfterEpoch)
                lastAfter := params.GetString(flags.SearchAfterFlag)
                if len(lastAfter) > 0 {
//                     nextAfterDate := time.Parse("2006-01-02T15:04:05Z", searchAfter)
                    last, e := strconv.ParseInt(lastAfter, 0, 64)
                    if e != nil {
                        fmt.Println("Cannot parse int last SearchAfterFlag")
                    }
                    lastAfterEpoch := time.Unix(last, 0)
                    if nextAfterDate != lastAfterEpoch {
                        fmt.Println("nextAfterEpoch != lastAfterEpoch")
                        fmt.Println("Setting ", nextAfterEpoch)
//                         searchAfterEpoch := nextAfterDate.UnixNano() / 1000000
                        params.Set(flags.SearchAfterFlag, nextAfterEpoch)
                    }
                }

                offset, _ := strconv.Atoi(params.GetString(flags.OffsetFlag))
                fmt.Println("OffsetFlag")
                fmt.Println(offset)

                if offset > 0  && offset/max > pagesNeeded {
                    fmt.Println("offset/max > pagesNeeded")
                    fmt.Println(offset/max > pagesNeeded)
                    break
                }
                offset = offset + max
                params.Set(flags.OffsetFlag, offset)
            }
        }else{
            fmt.Println("Response missing data field")
        }

    }

    //this is where we aggregate the results from each page
    // and append them to set decoded["data"]
    //we also need to set search_after based on the last result

    decoded["data"] = aggregateItems
	after := cli.HandleAfter(handlerPath, params, resp, decoded)
	if after != nil {
		decoded = after.(map[string]interface{})
	}

	return resp, decoded, nil
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
			"url":         "http://acf3425c8d41b11e9a12912cf37a7528-1694331899.us-east-1.elb.amazonaws.com/onestop-search",
		},
	}
}

func scdrOutputFormatAndPrint(decoded map[string]interface{}) {
	if output, ok := decoded["scdr-output"].([]string); ok {
		for _, row := range output {
			fmt.Println(strings.TrimSpace(row))
		}
	} else {
		fmt.Println("No results")
	}
}
