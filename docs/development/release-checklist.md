# Release Prep Checklist

## Code Verification

- [ ] Update libraries to latest, if possible
- [ ] Ensure that included images are compressed in the git repo

### Automated Code Verification

- [ ] Run `retire -p` on client code [Retire JS](https://retirejs.github.io/retire.js/)
  - This runs as part of the client-checks job in every circle build.
- [ ] Run `gradlew dependencyCheckAnalyze --info` to do OWASP security check
  - This runs nightly on the master branch.

## Documentation

- [ ] Review documentation, and that it is still in sync with the project.
- [ ] Confirm supported browser docs: [Supported Browsers](https://github.com/cedardevs/onestop/wiki/OneStop-Client-Supported-Browsers)
- [ ] Update the "as of" date in the supported browser docs.

## Test Environment

1. Deploy the latest master branch code to https://sciapps.colorado.edu (private deployOS playbook)
1. Reset the indices and reload the data. (private loadOnestop playbook)
    - [ ] Check the logs, to make sure when ETL runs, 100% of the collections and granules make it into staging (or document that those that do not are not expected to)

## Manual UI Checks

During all UI checks, make sure to keep an eye on the dev console for unexpected errors or warnings.

## Browser Support and CSS

- Verify CSS works across supported browsers:
  - note for css on IE (VirtualBox): You may have to manually clear the IE cache.
  - [ ] IE 11 on AdminLan
  - [ ] IE 11 on Windows
  - [ ] Firefox latest on Mac
  - [ ] Firefox latest on AdminLan
  - [ ] Chrome latest on Mac
  - [ ] Chrome latest on AdminLan
  - [ ] Safari
  - [ ] Use Chrome dev mobile view to check rendering on mobile devices, including landscape views
- Test responsiveness by adjusting window size between large and small
  - [ ] Landing page adjusts gracefully
  - [ ] Header and footer elements maintain reasonable placement in page layout
  - [ ] On search page, with all keyword categories expanded, they stay visible (the footer is pushed down without clipping)
  - [ ] Result cards respond to page adjustments
  - [ ] Collection detail view realigns elements cleanly on each tab

## UI Behavior

Follow these steps to confirm that core behavior is working as expected, as well as verify 508 compliance with some quick checks.

1. Open dev tools panel.
1. Load the landing page.
    - [ ] The DEMO disclaimer appears at the top of the page, in red
    - [ ] Correct version number is displayed in the footer
    - [ ] Links in the header menu and footer work
    - [ ] Wave/ANDI automated 508 checks
1. Go to the Help page
    - [ ] The quick help is still accurate
    - [ ] Wave/ANDI automated 508 checks
1. Go to the About Us page
    - [ ] It lists expected number of collections (400+) and granules (50+)
    - [ ] Wave/ANDI automated 508 checks
1. **keyboard** Return to the home page.
1. **mouse** Click `Weather` under Popular Topics.
    - [ ] 20 results should be showing, with the option to show more
1. **mouse** Click `Show More Results`
    - [ ] At the top, header is updated to reflect number of results shown
    - [ ] At the bottom, the show more button disappears when all results are loaded
1. Return to the landing page using the back button
1. **keyboard** Select `Weather` button again.
1. **keyboard** Tab to and click the `Show More Results` button.
    - [ ] Keyboard focus is easy to track throughout the page
    - [ ] All the weather results show images (or a black overlay if there is a problem loading it - this is common in the test environment and doesn't indicate a problem)
1. **mouse** Open the filters for Keywords and Data Theme
    - [ ] At the top level, themes look approximately like: (exact values may have changed)
        > - Agriculture (1)
        >  - Atmosphere (15)
        > - Biological Classification (1)
        > - Biosphere (1)
        > - Climate Indicators (1)
        > - Cryosphere (1)
        > - Oceans (23)
        > - Spectral/Engineering (1)
1. **keyboard** Expand keywords `Atmosphere` > `Atmospheric Winds` > `Surface Winds` > `U/V Wind Components`
    - Tab to keywords, then navigate with arrow keys.
    - [ ] Each level is formatted correctly
    - **keyboard** Select this filter.
1. **mouse** Expand Instruments.
    - [ ] Since the U/V Wind Components filter is on, some instruments listed have (0) results and are greyed out
    - [ ] `Anemometers` and `MOCNESS Plankton Net` have correct capitalization.
    - **mouse** Select these two options
    - [ ] Three applied filter bubbles should show above the results. Confirm all three are formatted correctly.
    - [ ] In the dev tools network tab, look for the last POST to /search/collection. The request payload should be: ```{"queries":[{"type":"queryText","value":"weather"}],"filters":[{"type":"facet","name":"science","values":["Atmosphere > Atmospheric Winds > Surface Winds > U/V Wind Components"]},{"type":"facet","name":"instruments","values":["ANEMOMETERS","MOCNESS > MOCNESS Plankton Net"]}],"facets":true,"page":{"max":20,"offset":0}}```
    - [ ] Wave/ANDI automated 508 checks
1. **mouse** Only one result should be showing. Select it.
    - [ ] URL changes to `collections/details/UUID`
    - [ ] Reloading the page or copying this URL into a new window loads the same view
    - [ ] This collection has 0 granules, so no link appears under `Files`
    - [ ] Time period on the summary tab is displayed as "1993-05-22 to 2006-12-02"
    - [ ] Bounding box on the summary tab is displayed as "Bounding box covering -172.99°, -78.652°, 113.3667°, 76.1552° (W, N, E, S)."
    - [ ] DSMM is displayed as "not available"
    - [ ] Access Tab should have sections for Information, Download Data, Search Data, Distribution Format. Only Download metadata should have a message such as "No links in metadata.", the others should have specific information
1. **keyboard** Tab to the summary tab, then to the `Show All` button underneath "Themes" and select it.
1. **mouse** Click `Show All` button underneath "Instruments".
    - [ ] All three keywords from the above filtering steps are displayed with the same formatting
1. Go back to the search results and expand the themes and instruments filters again.
    - [ ] **mouse** remove one of the filters by clicking the `x` on the filter bubble
    - [ ] **keyboard** tab to another filter bubble and remove it
    - [ ] remove the last filter by unselecting the checkbox in the left-hand filter menu
    - [ ] the instruments list expands to include more options
    - [ ] toggle the `U/V Wind Components` filter on and off to see it grey out some instruments, then re-enable them when the filter is removed
1. Go back to the landing page. Search for `Tutuila`. (There should be 4 results)
1. Using the time filter, enter the start year 2010. (**mouse** click on the `Apply` button).
    - [ ] Results are filtered down to 2
1. Remove the filter using the `Clear` button in the time filter form.
1. **keyboard** Enter the end date 1990 April 9.
    - [ ] Results are filtered down to 3
1. Enter the start year 2010 and apply the filter
    - [ ] A validation error displays
1. Clear the filters. Select `Pago Pago, American Samoa Tsunami Forecast Grids for MOST Model`
    - [ ] on the Access tab, Information (and Download Data) says `No links in metadata.`
1. In the header search bar, search for `"southern alaska coastal relief"`. (There should be 1 result)
1. **keyboard** Select this result
    - [ ] In the summary tab, location is "Bounding box covering 170°, 48.5°, 230°, 66.5° (W, N, E, S)."
    - [ ] In the map, the box is drawn around Alaska. (This renders across the dateline)
    - [ ] Access Tab should have sections Information, Download Data, Distribution Format. Note Search Data is not listed.
1. Search for `"GHRSST Level 4 ODYSSEA Mediterranean Sea Regional Foundation Sea Surface Temperature Analysis (GDS version 1)"` and select the result.
    - [ ] Location reads "Bounding box covering -18.5°, 30°, 36.5°, 46.5° (W, N, E, S)."
    - [ ] Map image shows enclosed area of Mediterranean region in Europe. (This renders across the prime meridian)
    - [ ] The DSMM shows 2.5 stars
1. Search for `water`. (There are over 300 results.)
    - [ ] **mouse** Using location filter, expand the map, draw a bounding box over the Mediterranean region. A new search triggers automatically. (There are less than 50 results - approximately 30)
    - [ ] **keyboard** In bounding box text fields, enter: -32, -1, 120, 60. (Similar number of results to above search, plus or minus a few).
    - [ ] The applied filter bubble updates the coordinates from the previous search to this one.
    - [ ] **keyboard** Apply the `Exclude Global` filter. This drops the result total by about half.
    - [ ] **mouse** Uncheck the `Exclude Global` checkbox.
1. Enter bounding coordinates West: -191, South: 90, East: 90, North: 90
    - [ ] A validation error displays
1. Delete the incorrect -191 but leave it blank and attempt to apply the filter.
    - [ ] A validation error displays
1. Change the West value to `foo`
    - [ ] A validation error displays
1. Search for `tree rings`. Select any of the paleo results.
    - [ ] The begin date shows with a negative year (eg -0106-01-01)
1. Search for `"NOAA/WDS Paleoclimatology - A global planktic foraminifer census data set for the Pliocene ocean"`. Select the result.
    - [ ] The date displays as "-617905000 to -1601050-12-31"
1. Search `glacier`. Select the first result (*not* the Digital Elevation Model).
    - [ ] Point geometry is rendered as "Point at -49.815°, 69.222° (longitude, latitude)"
    - [ ] Map displays a point in Greenland on the map
    - [ ] Only one instrument is listed (no "Show All" button)
1. Search `"WATER TEMPERATURE and other data from LITTLE ROCK"`.
    - [ ] The result card show in a map, since there is no thumbnail image.
    - Select the result.
    - [ ] In the description area, it reads "No description available" and there is no image where most other collections have one.
    - [ ] Line geometry is rendered as "Line from -7.7°, 51.5° (WS) to -7.7°, 51.6° (EN)."
    - [ ] Zoom in on the map to see that it is correctly displaying as a line.
1. Search `suvi`. Select the result with 6 solar images in the graphic.
    - [ ] No spatial bounding provided. World map is shown with no red bounding box.
    - [ ] Videos tab exists
    - [ ] Themes has only 3 keywords, and no show all link
    - [ ] Instrument and platforms both say 'none provided'
1. Search `co-ops`
    - [ ] filter the results with start date `2017`
    - and select the "Coastal meteorological and water temperature data from National Water Level Observation Network (NWLON)..." result.
    - [ ] Files section has a link indicating the collection has "10 matching of 22 total files" (wording TBD)
    - [ ] Wave/ANDI automated 508 checks
    - [ ] This collection has no end date, so displays as "2013-03-01 to Present"
    - **keyboard** Navigate to the Access Tab.
    - [ ] It has sections for Information, Download Data, Search Data, Distribution Format. All are populated
    - **mouse** Click `Show 10 of 22 matching files` (wording TBD)
    - [ ] 10 granules are shown, with a applied filter indicating the `2017` start date has carried over from the collection search. Remove the filter.
    - [ ] Use the browser back button to go back to before the collection search, then the forward button to return to this page in the history to confirm that no weird loading things prevent that from working.
    - [ ] Wave/ANDI automated 508 checks
    - [ ] 20 granules are shown, with a "show more" button at the bottom to load the rest
    - [ ] each granule has a map with a point shown
    - [ ] each granule has FTP, HTTP, and several THREDDS links
    - [ ] granules in both 2016 and 2017 are listed
    - [ ] filter granules with search `177000*` and `1770000`. Both should match 2 files. Applied filter bubble should read `Filename Contains: [search term]`.
    - [ ] filter granules with search `177000` - should match zero files.
    - clear the filters
    - [ ] apply location: 166, -25, -135, 35 - (18 results)
    - [ ] draw on the location filter a box containing all of the pacific ocean - it should match all 22 results (or draw it smaller to get various subsets)
    - clear the filters
    - [ ] apply the facet Link Protocols `UNIDATA:THREDDS` (20 results)
    - clear the filters
    - [ ] apply the facet Data Center - `Tidesandcurrents.Noaa.Gov` (2 results)
    - [ ] above the granule list is a link back to the collection. click it. It should take you back to the details page with no filters applied
1. Search `ghrsst viirs ACSPO`. The first or 2nd result should be "GHRSST GDS2 Level 2P Global Skin Sea Surface Temperature from the Visible Infrared Imaging Radiometer Suite (VIIRS) on the Suomi NPP satellite created by the NOAA Advanced Clear-Sky Processor for Ocean (ACSPO) (GDS version 2)" (orange image)
    - this result can also be found with `fileIdentifier:"gov.noaa.nodc:GHRSST-VIIRS_NPP-OSPO-L2P"`
    - Select this collection
    - [ ] Citation info is listed (expand Citation)
    - [ ] Identifier(s) section lists `gov.noaa.nodc:GHRSST-VIIRS_NPP-OSPO-L2P` and a doi link
    - [ ] **keyboard** Select the show matching files link
    - [ ] Files with a variety of bounding boxes appear
    - [ ] Access Protocols show Download, FTP, OPeNDAP, THREDDS and Web
1. Search `DEM`. One of those should be "Hilo, Hawaii 1/3 arc-second DEM"
    - Select this collection
    - [ ] On the Access Tab, there should be a 'Services' section. It has 3 categories with  map services (2 links), "Model Global Mosaic ArcGIS" image services (3 links), and "MHW Mosaic ArcGIS" image service (1 link) respectively.

1. In the search box:
    - [ ] Enter a search with blank text field (disallowed)
    - [ ] Enter a search starting with `*` (disallowed)
    - [ ] Enter a search starting with `?` (disallowed)
    - [ ] Enter a search with an escape character `\` or `/`. The error page is shown due to a parsing error.

## Live Docs Pages

- [ ] https://sciapps.colorado.edu/onestop/sitemap.xml
- [ ] At least one submap URL is listed. Paste it into the address bar.
- [ ] All the collections loaded are listed. Paste one into the address bar to load the details page, to ensure the sitemap links are formatted correctly.
- [ ] https://sciapps.colorado.edu/onestop-search/docs/openapi.yaml (should auto-download or display the yaml file, depending on browser)

### TBD

#### Data we should identify or add to the test set:

- No time range (in normal fields)
- Hazard image granules
- OER videos

#### Future Features?

- Paleo date filtering
- Time filtering with different relationships
