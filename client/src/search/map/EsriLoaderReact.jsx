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
  mapContainerStyle: { border: '1px solid #eee', minHeight: '400px', display: 'flex' },
  onError: (error, info) => console.error(error),
}

export default EsriLoaderReact
