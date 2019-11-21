package main

import (
	"fmt"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/pkg/errors"
	"github.com/rs/zerolog/log"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
	"strings"
)

func ScdrSearchFlattenedGranule(params *viper.Viper, body string) (*gentleman.Response, map[string]interface{}, error) {
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
	typeArg := params.GetString(typeFlag)
	if len(typeArg) == 0 {
		return params
	}
	viper.SetConfigName("scdr-files-config") // name of config file (without extension)
	viper.AddConfigPath("/etc/scdr-files/")  // path to look for the config file in
	viper.AddConfigPath("$HOME/.scdr-files") // call multiple times to add many search paths
	viper.AddConfigPath(".")                 // optionally look for config in the working directory
	err := viper.ReadInConfig()              // Find and read the config file
	if err != nil {                          // Handle errors reading the config file
		panic(fmt.Errorf("Fatal error config file: %s \n", err))
	}

	scdrTypeIds := viper.Get("scdr-files").(map[string]interface{})
	// fmt.Println(scdrTypeIds["C01501"])
	uuid := scdrTypeIds[strings.ToLower(typeArg)]
	params.Set("type", uuid)
	return params
}

func buildRequest(params *viper.Viper) *gentleman.Request {
	server := viper.GetString("server")
	if server == "" {
		server = openapiServers()[viper.GetInt("server-index")]["url"]
	}

	isSummaryWithType := params.GetString("available") == "true" && len(params.GetString("type")) > 0

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
		collectionId := params.GetString("type")
		endpoint = "/collection/" + collectionId
	} else if params.GetString("available") == "true" {
		endpoint = "/search/collection"
	}
	return endpoint
}

func scdrRegister() {
	root := cli.Root

	cli.Root.Short = "SCDR OneStop Search API"
	cli.Root.Long = cli.Markdown("Search Collections and Granules! More information on search request and responses available at [Search API Requests](https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Requests) and [Search API Responses](https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Responses).")

	func() {
		params := viper.New()

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

				_, decoded, err := ScdrSearchFlattenedGranule(params, body)
				if err != nil {
					log.Fatal().Err(err).Msg("Error calling operation")
				}
				// --available returns count, dont strip data response
				if params.GetString("available") == "false" {
					// links := []string
					if links, ok := decoded["links"].([]string); ok {
						// links = links.([]string)
						for _, link := range links {
							fmt.Println(strings.TrimSpace(link))
						}
					} else {
						fmt.Println("No results")
					}
				} else {
					if summary, ok := decoded["summary"].([]string); ok {
						// links = links.([]string)
						for _, row := range summary {
							fmt.Println(strings.TrimSpace(row))
						}
					}
					// fmt.Println(strings.TrimSpace(link))
					// if err := cli.Formatter.Format(decoded); err != nil {
					// 	log.Fatal().Err(err).Msg("Formatting failed")
					// }
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
