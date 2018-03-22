import React, {Component} from 'react'
import BrowserDetection from 'react-browser-detection'

import earth from '../../img/Earth.jpg'

const EARTH_HEIGHT = '30em'
const ARC_HEIGHT = '15em'

const styleEarth = {
  position: 'relative',
}

const styleEarthSVG = {
  flex: '0 0 auto',
  zIndex: 0,
  backgroundColor: 'none',
  height: EARTH_HEIGHT,
  position: 'absolute',
}

const styleEarthMargin = {
  marginTop: `calc(${EARTH_HEIGHT} - ${ARC_HEIGHT})`,
}

const styleEarthFallback = {
  flex: '0 0 auto',
  background: `url(${earth})`,
  backgroundSize: 'cover',
  height: ARC_HEIGHT,
}

const styleEarthFallbackGradient = {
  background:
    'linear-gradient(180deg, transparent 0%, transparent 90%, rgba(255,255,255, 0.5) 99%, white 100%)',
  height: ARC_HEIGHT,
}

export default class Earth extends Component {
  render() {
    const renderSVG = (
      <div style={styleEarth}>
        <svg
          width="100%"
          height={EARTH_HEIGHT}
          xmlns="http://www.w3.org/2000/svg"
          style={styleEarthSVG}
        >
          <defs>
            <pattern
              id="image"
              patternUnits="userSpaceOnUse"
              height={EARTH_HEIGHT}
              width="100%"
            >
              <image x="0" y="0" width="100%" xlinkHref={earth} />
            </pattern>
          </defs>
          <mask id="arc-mask">
            <rect width="100%" height={EARTH_HEIGHT} fill="white" />
            <ellipse cx="50%" cy="100%" rx="75%" ry={ARC_HEIGHT} fill="black" />
          </mask>
          <rect
            width="100%"
            height={EARTH_HEIGHT}
            fill="url(#image)"
            mask="url(#arc-mask)"
          />
        </svg>
        <div style={styleEarthMargin} />
      </div>
    )

    const renderFallback = (
      <div style={styleEarthFallback}>
        <div style={styleEarthFallbackGradient} />
      </div>
    )

    const browserHandler = {
      chrome: () => renderSVG,
      default: browser => renderFallback,
    }

    return <BrowserDetection>{browserHandler}</BrowserDetection>
  }
}
