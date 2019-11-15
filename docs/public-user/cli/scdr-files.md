# Other CLI tools

`scdr-files` was added as a subcommand to demonstrate how the OneStop API can be used to replace existing CLI tools while maintaining their programatic interface.

## scdr-files usage

Type -

`cli scdr-files --type="gov.noaa.nodc:NDBC-COOPS"`

Date -

`cli scdr-files --date=2016/03/02`

and without year (defaults to current year)-

`cli scdr-files --date=10/01`

Area-

`cli  scdr-files --area="POLYGON(( 22.686768 34.051522, 30.606537 34.051522, 30.606537 41.280903,  22.686768 41.280903, 22.686768 34.051522 ))"`

Text Query -

`cli  scdr-files --query="parentIdentifier:/.*NDBC-COOPS/"`
