package main

import (
	"crypto/tls"
	"github.com/danielgtaylor/openapi-cli-generator/cli"
	gtls "gopkg.in/h2non/gentleman.v2/plugins/tls"
)

func main() {
	cli.Init(&cli.Config{
		AppName:   "onestop-cli",
		EnvPrefix: "ONESTOP_CLI",
		Version:   "1.0.0",
	})

	cli.Client.Use(gtls.Config(&tls.Config{InsecureSkipVerify: true}))

	setScdrFlags()
	scdrRegister()

	setOneStopFlags()
	openapiRegister(false)

	cli.Root.Execute()
}
