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

func scdrRegister() {
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

		//scdrExampleCommands ini scdr-files.go
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
	typeArg := params.GetString(typeFlag)
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
	if server == "" {
		server = openapiServers()[viper.GetInt("server-index")]["url"]
	}

	isSummaryWithType := params.GetString(availableFlag) == "true" && len(params.GetString(typeFlag)) > 0

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
		collectionId := params.GetString(typeFlag)
		endpoint = "/collection/" + collectionId
	} else if params.GetString(availableFlag) == "true" {
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
