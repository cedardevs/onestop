import React from 'react'

import BrowserDetection from 'react-browser-detection'

import earth from '../../../img/Earth.jpg'

const DEFAULT_GRAPHIC = earth
const DEFAULT_HEIGHT = '30em'
const DEFAULT_ARC_HEIGHT = '15em'

const styleWrapper = {
  position: 'relative',
}

const styleSVG = height => {
  return {
    flex: '0 0 auto',
    zIndex: 0,
    backgroundColor: 'none',
    height: height,
    position: 'absolute',
  }
}

const styleMargin = (height, arcHeight) => {
  return {
    marginTop: `calc(${height} - ${arcHeight})`,
  }
}

const styleFallback = (graphic, arcHeight) => {
  return {
    flex: '0 0 auto',
    background: `url(${graphic})`,
    backgroundSize: 'cover',
    height: arcHeight,
  }
}

const styleFallbackGradient = arcHeight => {
  return {
    background:
      'linear-gradient(180deg, transparent 0%, transparent 90%, rgba(255,255,255, 0.5) 99%, white 100%)',
    height: arcHeight,
  }
}

export default class Banner extends React.Component {
  render() {
    const {graphic, height, arcHeight, visible} = this.props

    if (!visible) {
      return null
    }

    const bannerGraphic = graphic ? graphic : DEFAULT_GRAPHIC
    const bannerHeight = height ? height : DEFAULT_HEIGHT
    const bannerArcHeight = arcHeight ? arcHeight : DEFAULT_ARC_HEIGHT

    const renderSVG = (
      <div style={styleWrapper}>
        <svg
          focusable="false"
          width="100%"
          height={bannerHeight}
          xmlns="http://www.w3.org/2000/svg"
          style={styleSVG(bannerHeight)}
        >
          <defs>
            <pattern
              id="image"
              patternUnits="userSpaceOnUse"
              height={bannerHeight}
              width="100%"
            >
              <image x="0" y="0" width="100%" xlinkHref={bannerGraphic} />
            </pattern>
          </defs>
          <mask id="arc-mask">
            <rect width="100%" height={bannerHeight} fill="white" />
            <ellipse
              cx="50%"
              cy="100%"
              rx="75%"
              ry={bannerArcHeight}
              fill="black"
            />
          </mask>
          <rect
            width="100%"
            height={bannerHeight}
            fill="url(#image)"
            mask="url(#arc-mask)"
          />
        </svg>
        <div style={styleMargin(bannerHeight, bannerArcHeight)} />
      </div>
    )

    const renderFallback = (
      <div style={styleFallback(bannerGraphic, bannerArcHeight)}>
        <div style={styleFallbackGradient(bannerArcHeight)} />
      </div>
    )

    const browserHandler = {
      chrome: () => renderSVG,
      default: browser => renderFallback,
    }

    return <BrowserDetection>{browserHandler}</BrowserDetection>
  }
}
