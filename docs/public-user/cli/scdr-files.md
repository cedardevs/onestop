# Other CLI tools

`scdr-files` was added as a subcommand to demonstrate how the OneStop API can be used to replace existing CLI tools while maintaining their programatic interface.

## scdr-files configurations

Config locations -

Users can supply a configuration to map scdr-file type short names to OneStop IDs. See default config in [default config](cli/scdr-files-config.yaml). File can be yaml or json, but must be named "scdr-files-config" and placed in one of the following locations-  project directory, current working directory, /etc/scdr-files/, or $HOME/.scdr-files.


## scdr-files usage

Help for a full list of available flags and examples -

`cli scdr-files --help`

Type -

`cli scdr-files --type abi-l1b-rad`

`cli scdr-files -t abi-l1b-rad`

Date -

`cli scdr-files --date 2016/03/02`

and without year (defaults to current year)-

`cli scdr-files --date 10/01`

Area-

`cli scdr-files --area "POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"`

Text Query -

`cli scdr-files -q "gcmdPlatforms:/GOES-16.*/"`

`cli scdr-files --query "parentIdentifier:/.*NDBC-COOPS/"`

<hr>
<div align="center"><a href="/onestop/public-user/cli/quickstart">Previous</a> | <a href="#">Top of Page</a> | <a href="/onestop/public-user">Next</a></div>
