package main

import (
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/pkg/errors"
	"github.com/rs/zerolog/log"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"gopkg.in/h2non/gentleman.v2"
)

func ScdrSearchFlattenedGranule(params *viper.Viper, body string) (*gentleman.Response, map[string]interface{}, error) {
	handlerPath := "scdr-files"
	server := viper.GetString("server")
	if server == "" {
		server = openapiServers()[viper.GetInt("server-index")]["url"]
	}

	url := server + "/search/flattened-granule"

	req := cli.Client.Post().URL(url)

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

func scdrRegister() {
	root := cli.Root

	cli.Root.Short = "SCDR OneStop Search API"
	cli.Root.Long = cli.Markdown("Search Collections and Granules! More information on search request and responses available at [Search API Requests](https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Requests) and [Search API Responses](https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Responses).")

	func() {
		params := viper.New()

		var examples string

		cmd := &cobra.Command{
			Use:     "scdr-files",
			Short:   "An SCDR interface for OneStop",
			Long:    cli.Markdown("Retrietve flattened granule metadata records matching the text query string, spatial, and/or temporal filter.\n## Request Schema (application/json)\n\nadditionalProperties: false\ndescription: The shape of a search request body that can be sent to the OneStop API\n  to execute a search against available metadata.\nproperties:\n  facets:\n    default: false\n    description: Flag to request counts of results by GCMD keywords in addition to\n      results.\n    type: boolean\n  filters:\n    description: filters applied to the search\n    items:\n      oneOf:\n      - $ref: '#/components/schemas/dateTimeFilter'\n      - $ref: '#/components/schemas/facetFilter'\n      - $ref: '#/components/schemas/geometryFilter'\n      - $ref: '#/components/schemas/excludeGlobalFilter'\n      - $ref: '#/components/schemas/collectionFilter'\n    type: array\n  page:\n    $ref: '#/components/schemas/page'\n  queries:\n    description: List of queries to search against.\n    items:\n      oneOf:\n      - $ref: '#/components/schemas/textQuery'\n    type: array\n  summary:\n    default: true\n    description: Flag to request summary of search results instead of full set of\n      attributes.\n    type: boolean\ntype: object\n"),
			Example: examples,
			Args:    cobra.MinimumNArgs(0),
			Run: func(cmd *cobra.Command, args []string) {
				body, err := cli.GetBody("application/json", args[0:])
				if err != nil {
					log.Fatal().Err(err).Msg("Unable to get body")
				}

				_, decoded, err := ScdrSearchFlattenedGranule(params, body)
				if err != nil {
					log.Fatal().Err(err).Msg("Error calling operation")
				}

				if err := cli.Formatter.Format(decoded); err != nil {
					log.Fatal().Err(err).Msg("Formatting failed")
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
