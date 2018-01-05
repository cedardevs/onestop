import React from 'react'
import EsriLoaderReact from './EsriLoaderReact'
import ArcGISUtils from '../../utils/ArcGISUtils'

const WKID = 4326

const options = {
  url: 'https://js.arcgis.com/4.5/',
}

const styleMapContainer = showMap => {
  return {
    boxSizing: 'border-box',
    backgroundColor: '#6095C8',
    transition: showMap ? 'height 0.2s 0.0s, padding 0.1s 0.2s, width 0.2s 0.3s' : 'width 0.2s 0.0s, padding 0.1s 0.2s, height 0.2s 0.3s',
    padding: showMap ? '1em' : '0em',
    height: showMap ? '400px' : '0px',
    width: showMap ? '100%' : '0%'
  }
}

const styleMap = showMap => {
  return {
    zIndex: 2,
    padding: 0,
    margin: 0,
    display: showMap ? 'flex' : 'none',
    position: 'relative',
    height: '100%',
    alignItems: 'flex-start',
  }
}

export default class ArcGISMap extends React.Component {

  componentWillReceiveProps(nextProps) {
    const { bounds } = this.props
    if(nextProps.bounds !== bounds) {
    }
  }

  render() {
    const { showMap, bounds, boundsSource, updateBounds } = this.props

    const mapView = (
      <EsriLoaderReact
        options={options}
        modulesToLoad={[
          'esri/Map',
          'esri/views/MapView',
          'esri/widgets/ScaleBar',
          'esri/layers/GraphicsLayer',
          'esri/Graphic',
          'esri/geometry/support/webMercatorUtils',
          'esri/geometry/Extent',
          'esri/geometry/geometryEngine',
          'dojo/domReady!',
        ]}
        onReady={({
          loadedModules: [
            Map,
            MapView,
            ScaleBar,
            GraphicsLayer,
            Graphic,
            webMercatorUtils,
            Extent,
            geometryEngine
          ],
          containerNode,
        }) => {

          let map = new Map(ArcGISUtils.map.initialConditions())
          let mapGraphicsLayer = new GraphicsLayer()
          map.add(mapGraphicsLayer)
          let view = new MapView(ArcGISUtils.mapView.initialConditions(containerNode, map))

          let extentGraphic = null
          let textGraphic = null
          let origin = null
          let extent = null
          let newBounds = null

          // UPDATE EXTENT WHEN DRAGGING
          view.on('drag', event => {
            event.stopPropagation()

            if (event.action === 'start') {
              if (mapGraphicsLayer) mapGraphicsLayer.removeAll()
              origin = view.toMap(event)
            } else if (event.action === 'update') {
              if (mapGraphicsLayer) mapGraphicsLayer.removeAll()
              let point = view.toMap(event)
              extent = new Extent(ArcGISUtils.extent.initialConditions(origin,point))
              newBounds = ArcGISUtils.extent.toBounds(extent, webMercatorUtils)

              if(updateBounds) {
                updateBounds(newBounds, "map")
              }

            }
          })

          // DRAW ON MAP
          if(extent) {
            let area = Number(geometryEngine.geodesicArea(extent, 'square-kilometers'))
            extentGraphic = new Graphic({
              geometry: extent,
              symbol: ArcGISUtils.extent.fillSymbol(),
            })

            let commaNumberPosition = /\B(?=(\d{3})+(?!\d))/g
            let areaText = `${area.toFixed(2).toString().replace(commaNumberPosition, ",")} km²`
            textGraphic = new Graphic({
              geometry: ArcGISUtils.extent.textGeometry(bounds),
              symbol: ArcGISUtils.extent.textSymbol(areaText)
            })

            mapGraphicsLayer.add(extentGraphic)
            mapGraphicsLayer.add(textGraphic)
          }

          let scaleBar = new ScaleBar({
            view: view,
          })
          let scaleBarOptions = {
            position: 'bottom-left',
          }

          view.ui.add(scaleBar, scaleBarOptions)

          view.constraints = {
            minZoom: 2,
            maxZoom: 8,
            rotationEnabled: false,
          }
        }}
      />
    )

    return (
      <div style={styleMapContainer(showMap)}>
        <div style={styleMap(showMap)}>{mapView}</div>
      </div>
    )
  }
}
