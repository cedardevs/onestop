export const mockConfigResponse = {
  banner: {
    message:
      "DEMO - This site is not running on NCEI hardware, does not contain NCEI's full data holdings, and contains a limited set of its intended functionality.",
    colors: null,
  },
  featured: [
    {
      title: 'GOES Data',
      searchTerm: '"Gridded Satellite GOES"',
      imageUrl: 'https://www.ncdc.noaa.gov/gridsat/images/sample.png',
    },
    {
      title: 'Digital Elevation Models',
      searchTerm: '"digital elevation"',
      imageUrl:
        'https://gis.ngdc.noaa.gov/arcgis/rest/services/DEM_global_mosaic_hillshade/ImageServer/exportImage?bbox=-170.95,-14.40,-170.45,-14.18&size=500,500&format=png32&interpolation=%20RSP_BilinearInterpolation&renderingRule=%7B%22rasterFunction%22:%22ColorHillshade%22%7D&f=image',
    },
    {
      title: 'NWLON and PORTS',
      searchTerm: '+nwlon +ports',
      imageUrl:
        'https://data.nodc.noaa.gov/cgi-bin/gfx?id=gov.noaa.nodc:NDBC-COOPS',
    },
    {
      title: 'Climate Data Record (CDR)',
      searchTerm: '"NOAA Climate Data Record"',
      imageUrl:
        'https://www.ncdc.noaa.gov/sites/default/files/styles/cdr-full-width/public/cdr/AVHRRSurfaceReflectance.png',
    },
  ],
  enabledFeatureToggles: null,
}
