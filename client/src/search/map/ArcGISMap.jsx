import React from 'react'
import EsriLoaderReact from './EsriLoaderReact'


const WKID = 4326;

const options = {
  url: 'https://js.arcgis.com/4.5/',
}

const styleMap = {
  width: '100%',
}

export default class ArcGISMap extends React.Component {

  render() {
    const mapView = (
      <EsriLoaderReact
        options={options}
        modulesToLoad={[
          'esri/Map',
          'esri/views/MapView',
          'esri/widgets/ScaleBar',
          'esri/widgets/BasemapGallery',
          'esri/layers/GraphicsLayer',
          'esri/Graphic',
          'esri/Color',
          'esri/geometry/SpatialReference',
          'esri/geometry/Extent',
          'esri/symbols/Font',
          'esri/symbols/TextSymbol',
          'dojo/domReady!'
        ]}
        onReady={({
          loadedModules: [Map, MapView, ScaleBar, BasemapGallery, GraphicsLayer, Graphic, Color, SpatialReference, Extent, Font, TextSymbol],
          containerNode,
        }) => {

          // https://developers.arcgis.com/javascript/latest/api-reference/esri-Map.html
          let mapOptions = {
            basemap: 'hybrid'
          }

          let map = new Map(mapOptions)
          let mapGraphicsLayer = new GraphicsLayer()
          map.add(mapGraphicsLayer)

          let view = new MapView({
            center: [-80, 35],
            container: containerNode,
            map: map,
            zoom: 3
          })

          // create a symbol for rendering the graphic
          let fillSymbol = {
            type: 'simple-fill', // autocasts as new SimpleFillSymbol()
            color: [255, 255, 255, 0.309],
            outline: { // autocasts as new SimpleLineSymbol()
              color: [255, 255, 255],
              width: 1
            }
          }

          var textSymbol = {
            type: "text",  // autocasts as new TextSymbol()
            color: "white",
            haloColor: "black",
            haloSize: "1px",
            text: "You are here",
            xoffset: 3,
            yoffset: 3,
            font: {  // autocast as new Font()
              size: 12,
              family: "sans-serif",
              weight: "bolder"
            }
          };

          let extentGraphic = null
          let textGraphic = null
          let origin = null

          view.on('drag', event => {
            event.stopPropagation()
            if(event.action === 'start') {
              if(mapGraphicsLayer) mapGraphicsLayer.removeAll()
              if(extentGraphic) view.graphics.remove(extentGraphic)
              // if(textGraphic) view.graphics.remove(textGraphic)
              origin = view.toMap(event)
            } else if(event.action === 'update') {
              if(mapGraphicsLayer) mapGraphicsLayer.removeAll()
              if(extentGraphic) view.graphics.remove(extentGraphic)
              // if(textGraphic) view.graphics.remove(textGraphic)
              let p = view.toMap(event)
              extentGraphic = new Graphic({
                geometry: new Extent({
                  xmin: Math.min(p.x, origin.x),
                  xmax: Math.max(p.x, origin.x),
                  ymin: Math.min(p.y, origin.y),
                  ymax: Math.max(p.y, origin.y),
                  spatialReference: { wkid: 102100 }
                }),
                symbol: fillSymbol
              })

              mapGraphicsLayer.add(extentGraphic)
              // view.graphics.add(extentGraphic)


              // let font = new Font('10px', Font.STYLE_NORMAL, Font.VARIANT_NORMAL, Font.WEIGHT_BOLDER)
              // let textSymbol = new TextSymbol('booya', font, new Color([0, 0, 0]))
              // let textPoint = {
              //   type: 'point',
              //   longitude: origin.y,
              //   latitude: origin.x
              // }
              // // let textPoint = new Point(origin.y, origin.x, new SpatialReference({wkid: WKID}))
              // textGraphic = new Graphic({
              //   geometry: textPoint,
              //   symbol: textSymbol
              // })
              // mapGraphicsLayer.add(textGraphic)


            }
          })

          let scaleBar = new ScaleBar({
            view: view
          })
          let scaleBarOptions = {
            position: 'bottom-left'
          }

          view.ui.add(scaleBar, scaleBarOptions)

          // let basemapGallery = new BasemapGallery({
          //   view: view
          // })
          //
          // let basemapGalleryOptions = {
          //   position: 'top-right'
          // }
          //
          // view.ui.add(basemapGallery, basemapGalleryOptions)

          view.constraints = {
            minZoom: 2,
            maxZoom: 8,
            rotationEnabled: false,
          }

        }}
      />
    )

    return <div style={styleMap}>{mapView}</div>
  }
}
