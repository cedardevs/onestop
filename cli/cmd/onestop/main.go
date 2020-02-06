package main

import (
	"crypto/tls"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	"github.com/CEDARDEVS/onestop/cli/internal/app/onestop"
	"github.com/CEDARDEVS/onestop/cli/internal/app/scdr"
	"github.com/CEDARDEVS/onestop/cli/internal/app/generated"
	gtls "gopkg.in/h2non/gentleman.v2/plugins/tls"
)

func main() {
	cli.Init(&cli.Config{
		AppName:   "onestop-cli",
		EnvPrefix: "ONESTOP_CLI",
		Version:   "1.0.0",
	})

	cli.Client.Use(gtls.Config(&tls.Config{InsecureSkipVerify: true}))

	//scdr-files.go
	scdr.SetScdrFlags()
	scdr.ScdrRegister()

	//onestop-flags.go
	onestop.SetOneStopFlags()

	//openapi.go
	generated.OpenapiRegister(false)

	cli.Root.Execute()
}
