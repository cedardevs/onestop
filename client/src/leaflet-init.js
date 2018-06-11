import L from 'leaflet'

L.SVG.addInitHook(function(){
  this.on('add', function(){
    // fix svg focus issue in IE
    this._container.setAttribute('focusable', 'false')
  })
})
