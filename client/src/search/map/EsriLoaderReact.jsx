import React from 'react'
import PropTypes from 'prop-types'
import { loadModules } from 'esri-loader'

class EsriLoaderReact extends React.PureComponent {
  componentDidCatch(error, info) {
    const { onError } = this.props
    if (onError) {
      onError(error, info)
    }
  }

  componentDidMount() {
    const { modulesToLoad, options, onReady, onError } = this.props
    loadModules(modulesToLoad ? modulesToLoad : [], options)
      .then(loadedModules => {
        if (onReady) {
          onReady({ loadedModules, containerNode: this.mapContainer })
        }
      })
      .catch(error => {
        if (onError) {
          onError(error, null)
        }
      })
  }

  render() {
    // return <div style={{backgroundColor: "magenta", width: '100%', height: "50px"}} />
    const { renderMapContainer, mapContainerStyle, children } = this.props
    if (!renderMapContainer) {
      return children
    }
    return (
      <div
        ref={node => (this.mapContainer = node)}
        style={mapContainerStyle}
      >
        {children}
      </div>
    )
  }
}

EsriLoaderReact.propTypes = {
  renderMapContainer: PropTypes.bool, // default is true
  mapContainerStyle: PropTypes.object,
  moduleToLoad: PropTypes.arrayOf(PropTypes.string),
  options: PropTypes.shape({
    url: PropTypes.string,
    dojoConfig: PropTypes.object,
  }),
  onError: PropTypes.func, // (error, info) =>
  onReady: PropTypes.func, // ({loadedModules, containerNode (null if renderMapContainer !== true)})
}

EsriLoaderReact.defaultProps = {
  renderMapContainer: true,
  mapContainerStyle: { position: 'absolute', width: '100%', height: '100%', margin: 0, padding: 0, backgroundColor: "#111" },
  onError: (error, info) => console.error(error),
}

export default EsriLoaderReact
