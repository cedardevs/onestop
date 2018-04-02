import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {processUrl} from '../../utils/urlUtils'
import MapThumbnail from '../../common/MapThumbnail'
import {boxShadow} from '../../common/defaultStyles'

// TODO: finish styling this component if we want an alternate list view for collection results
// when done, add ListItemComponent={CollectionListResult} as a prop to ListView in Collections.jsx

const styleResult = {
  // margin: '0 2em 2em 0',
  // textAlign: 'center',
}

// const styleContent = {
//   boxSizing: 'border-box',
//   width: '100%',
//   height: '100%',
//   color: 'white',
//   overflow: 'hidden',
//   position: 'relative',
//   boxShadow: boxShadow,
// }

const styleContent = {
  padding: '1.618em',
}

const styleOverlay = {
  position: 'absolute',
  top: 0,
  left: 0,
  bottom: 0,
  right: 0,
  display: 'inline-flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  background: 'none',
  width: '100%',
  height: '100%',
  boxSizing: 'content-box',
  border: 0,
  color: 'inherit',
  font: 'inherit',
  lineHeight: 'normal',
  overflow: 'visible',
  borderRadius: 0,
  padding: 0,
  margin: 0,
}

const styleOverlayHover = {
  color: 'white',
}

const styleOverlayFocus = {
  color: 'white',
}

const styleOverlayBlur = {
  color: 'inherit',
}

const styleArch = {
  position: 'absolute',
  boxSizing: 'border-box',
  width: '100%',
  bottom: 0,
  left: 0,
  right: 0,
  height: '4.472em',
  overflow: 'hidden',
  whiteSpace: 'nowrap',
  textOverflow: 'ellipsis',
  fontWeight: 'normal',
  padding: '0.618em 1em 0.618em 1em',
  margin: 0,
  color: '#222',
  backgroundColor: '#FBFBFB',
  transition: 'background-color 0.3s ease, color 0.3s ease, height 0.3s ease',
  borderTop: '1px solid #AAA',
  borderRadius: '12.5em 12.5em 0em 0em / 2.236em',
  boxShadow: boxShadow,
}

const styleArchHover = {
  fontWeight: 'bold',
  backgroundColor: '#327CAC',
  color: '#FBFBFB',
  height: '7.708em',
}

const styleArchFocus = {
  fontWeight: 'bold',
  backgroundColor: '#327CAC',
  color: '#FBFBFB',
  height: '7.708em',
}

const styleArchBlur = {
  fontWeight: 'normal',
  backgroundColor: '#FBFBFB',
  color: '#222',
  height: '4.472em',
}

const styleMapContainer = {
  position: 'absolute',
  top: 0,
  zIndex: 0,
  // width: '100%',
  // maxWidth: '100%',
  height: '10em',
}

const styleSuperTitle = {
  marginTop: '0.309em',
  marginBottom: '0.309em',
  fontSize: '1em',
  lineHeight: '1.618em',
  fontWeight: 'normal',
}

const styleTitle = {
  fontSize: '1em',
  textAlign: 'left',
  lineHeight: '1.618em', // use this value to count block height
  maxHeight: '4.854em', // maxHeight = lineHeight (1.618) * max lines (3)
  marginTop: '0.309em',
  marginBottom: '0.309em',
  textOverflow: 'ellipsis',
  whiteSpace: 'normal',
  overflow: 'hidden',
}

export default class CollectionListResult extends Component {
  constructor(props) {
    super(props)
    const {item} = this.props
    this.thumbnailUrl = processUrl(item.thumbnail)
  }

  componentWillMount() {
    this.setState(prevState => {
      return {
        hovering: false,
        focusing: false,
      }
    })
  }

  thumbnailStyle() {
    if (this.thumbnailUrl) {
      return {
        background: `url('${this.thumbnailUrl}')`,
        backgroundColor: 'black',
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'cover',
        backgroundPosition: 'center center',
      }
    }
  }

  handleKeyPress(event, actionHandler) {
    if (event.key == 'Enter') {
      actionHandler()
    }
  }

  renderThumbnailMap() {
    const {item} = this.props
    const geometry = item.spatialBounding
    if (!this.thumbnailUrl) {
      return (
        <div style={styleMapContainer}>
          <MapThumbnail geometry={geometry} interactive={false} />
        </div>
      )
    }
  }

  handleMouseOver = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
      }
    })
  }

  handleMouseOut = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
      }
    })
  }

  handleFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {item, onClick} = this.props

    const title = item.title

    const styleContentMerged = {
      ...styleContent,
      ...this.thumbnailStyle(),
    }

    // const styleOverlayMerged = {
    //   ...styleOverlay,
    //   ...(this.state.focusing ? styleOverlayFocus : styleOverlayBlur),
    //   ...(this.state.hovering ? styleOverlayHover : {}),
    // }

    // const styleArchMerged = {
    //   ...styleArch,
    //   ...(this.state.focusing ? styleArchFocus : styleArchBlur),
    //   ...(this.state.hovering ? styleArchHover : {}),
    // }

    return (
      <div
        style={styleResult}
        onKeyPress={e => this.handleKeyPress(e, onClick)}
      >
        <div style={styleContent}>
          <button
            style={{display: 'flex'}}
            onClick={onClick}
            onMouseOver={this.handleMouseOver}
            onMouseOut={this.handleMouseOut}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          >
            <div>IMAGE</div>
            <div>{title}</div>
          </button>
        </div>
      </div>
    )
  }
}

CollectionListResult.propTypes = {
  item: PropTypes.object.isRequired,
  onClick: PropTypes.func.isRequired,
}

// {this.renderThumbnailMap()}
// <div style={styleArchMerged}>
//   <div style={styleSuperTitle} aria-hidden={true}>
//     Collection Title:
//   </div>
//   <h2 style={styleTitle}>{title}</h2>
// </div>
