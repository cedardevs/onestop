import React from 'react'
import FlexRow from '../common/FlexRow'
import FlexColumn from '../common/FlexColumn'
import {processUrl} from '../utils/urlUtils'
import {fontFamilySerif} from '../utils/styleUtils'
import Button from '../common/input/Button'

import pause from 'fa/pause-circle-o.svg'
import play from 'fa/play-circle-o.svg'

const styleFeaturedDatasetsWrapper = {
  marginTop: '2.618em',
}

const styleFeaturedDatasetsLabel = {
  textAlign: 'center',
  fontFamily: fontFamilySerif(),
  margin: '0 0 0.618em 0',
}

const styleFeaturedDatasets = {
  color: '#F9F9F9',
}

const styleTitle = (active, first, last, collapseImage) => {
  const topRightRadius = collapseImage ? '1em' : '0'
  const bottomRightRadius = collapseImage ? '1em' : '0'
  return {
    background: active ? '#263f78' : '#026dab',
    textAlign: collapseImage ? 'center' : 'right',
    fontFamily: fontFamilySerif(),
    fontSize: '1.25em',
    padding: '.609em',
    borderRadius: first
      ? `1em ${topRightRadius} 0 0`
      : last ? `0 0 ${bottomRightRadius} 1em` : 'none',
  }
}

const styleTitleList = collapseImage => {
  return {
    listStyleType: 'none',
    cursor: 'pointer',
    margin: 0,
    padding: 0,
    width: collapseImage ? '100%' : '33%',
  }
}

const styleImageContainer = {
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  flex: 1,
  borderRadius: '0 1em 1em 1em',
  background: '#263f78',
  cursor: 'pointer',
  width: '66%',
  height: '35em',
  padding: '1em',
}

const styleImage = {
  display: 'flex',
  width: '100%',
  height: '100%',
  justifyContent: 'center',
}

const styleFeaturedImage = backgroundURL => {
  return {
    flex: 1,
    alignSelf: 'stretch',
    background: `url(${backgroundURL})`,
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'contain',
    backgroundPosition: 'center',
  }
}

const styleFeaturedButton = (active, first, last, collapseImage) => {
  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    width: '100%',
    background: 'transparent',
    color: 'inherit',
    outline: 'none',
    textAlign: collapseImage ? 'center' : 'right',
    justifyContent: collapseImage ? 'center' : 'flex-end',
    padding: '.309em',
    margin: first
      ? `0 .309em .309em .309em`
      : last ? `.309em .309em 0 .309em` : '.309em',
  }
}

const stylePlayPauseButton = {
  fontSize: '1em',
}

const stylePlayPauseButtonWrapper = collapseImage => {
  return {
    display: 'flex',
    justifyContent: 'flex-end',
    paddingTop: '.309em',
    paddingRight: !collapseImage ? '.309em' : '0',
  }
}

const stylePlayPauseButtonFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.309em',
  zIndex: '1',
}

const stylePlayPauseButtonIcon = {
  width: '1.3em',
  height: '1.3em',
  paddingTop: '0.309em',
  paddingBottom: '0.309em',
}

const styleFeaturedButtonFocus = {
  outline: '2px dashed white',
  textDecoration: 'underline',
  background: 'transparent',
}

const styleFeaturedButtonHover = {
  textDecoration: 'underline',
  background: 'transparent',
}

const Timer = function(callback, delay){
  var timerId,
    start,
    remaining = delay

  this.pause = () => {
    window.clearTimeout(timerId)
    remaining -= new Date() - start
  }

  this.resume = () => {
    start = new Date()
    window.clearTimeout(timerId)
    timerId = window.setTimeout(callback, remaining)
  }

  this.resume()
}

class FeaturedDatasets extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      current: 0,
      carouselLength: 0,
      timer: undefined,
      collapseImage: false,
      hovering: false,
      focusing: false,
      manualPause: false,
    }
  }

  isPaused = () => {
    const {focusing, hovering, manualPause} = this.state
    return !focusing && !hovering && !manualPause
  }

  search = query => {
    const {submit, updateQuery} = this.props
    updateQuery(query)
    submit(query)
  }

  togglePause = () => {
    if (this.isPaused()) {
      this.state.timer.resume()
    }
  }

  toggleManualPause = () => {
    this.state.timer.pause()
    this.setState(prevState => {
      return {
        ...prevState,
        manualPause: !prevState.manualPause,
      }
    }, this.togglePause)
  }

  handleBlur = () => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
        focusingCurrent: null,
      }
    }, this.togglePause)
  }

  handleFocus = i => {
    this.state.timer.pause()
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
        focusingCurrent: i,
        current: i,
      }
    }, this.togglePause)
  }

  handleMouseOut = () => {
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: false,
        current: prevState.focusing
          ? prevState.focusingCurrent
          : prevState.current,
      }
    }, this.togglePause)
  }

  handleMouseOver = i => {
    this.state.timer.pause()
    this.setState(prevState => {
      return {
        ...prevState,
        hovering: true,
        current: i,
      }
    }, this.togglePause)
  }

  setupTimer = items => {
    const {carouselLength, timer} = this.state

    if (items && (items.length || carouselLength !== items.length)) {
      this.setState({carouselLength: items.length})

      if (!timer) {
        this.setTimerState()
      }
    }
  }

  setTimerState = () => {
    this.setState({
      timer: new Timer(() => this.rotateCarousel(), 5000),
    })
  }

  rotateCarousel = () => {
    const {carouselLength, current} = this.state
    if (this.isPaused()) {
      const newCurrent = (current + 1) % carouselLength
      this.setState({current: newCurrent})
    }
    else {
    }
    this.setTimerState()
  }

  componentWillReceiveProps({featured}) {
    this.setupTimer(featured)
  }

  componentDidMount() {
    const {featured} = this.props
    this.setupTimer(featured)

    // need to set dimensions intially before resize events
    this.debounceResize()
    // subsequent resize event will retrigger our calculation
    window.addEventListener('resize', this.debounceResize)
  }

  componentWillUnmount() {
    if (this.state.timer) {
      this.state.timer.pause()
    }

    // remember to remove custom listeners before unmounting
    window.removeEventListener('resize', this.debounceResize)
  }

  debounceResize = () => {
    // prevent resize work unless threshold is reached
    // this helps ensure the new width doesn't stick on a transient value
    const resizeThreshold = 250 // ms
    clearTimeout(this.resizeId)
    this.resizeId = setTimeout(this.resize, resizeThreshold)
  }

  resize = () => {
    // do work only if iframe exists
    if (this.featuredRef) {
      // get new width dynamically
      const featuredRect = this.featuredRef.getBoundingClientRect()
      const newWidth = parseFloat(featuredRect.width)
      // if the image's new width goes below our threshold, collapse
      this.setState(prevState => {
        return {
          ...prevState,
          collapseImage: newWidth < 700,
        }
      })
    }
  }

  render() {
    const {featured} = this.props
    const {current, collapseImage} = this.state

    const manualPauseLabel = this.isPaused()
      ? 'Pause Featured Datasets Animation'
      : 'Start Featured Datasets Animation'
    const manualPauseButton = (
      <Button
        title={manualPauseLabel}
        icon={this.isPaused() ? pause : play}
        onClick={this.toggleManualPause}
        style={stylePlayPauseButton}
        styleFocus={stylePlayPauseButtonFocus}
        styleIcon={stylePlayPauseButtonIcon}
        ariaSelected={!this.isPaused()}
      />
    )

    if (featured !== null && featured.length > 0) {
      const titleList = (
        <ul key="title-list" style={styleTitleList(collapseImage)}>
          {featured.map((f, i, arr) => {
            const active = current === i
            const first = i === 0
            const last = i === arr.length - 1
            return (
              <li
                style={styleTitle(active, first, last, collapseImage)}
                key={i}
                onMouseEnter={() => this.handleMouseOver(i)}
                onMouseLeave={this.handleMouseOut}
              >
                <Button
                  text={f.title}
                  title={`${f.title} Featured Data Search`}
                  onClick={() => this.search(f.searchTerm)}
                  onFocus={() => this.handleFocus(i)}
                  onBlur={this.handleBlur}
                  style={styleFeaturedButton(
                    active,
                    first,
                    last,
                    collapseImage
                  )}
                  styleHover={styleFeaturedButtonHover}
                  styleFocus={styleFeaturedButtonFocus}
                />
              </li>
            )
          })}
          <li style={stylePlayPauseButtonWrapper(collapseImage)}>
            {manualPauseButton}
          </li>
        </ul>
      )

      const imageContainer = (
        <div key="image-container" style={styleImageContainer}>
          {featured.map((f, i) => {
            const active = current === i
            if (active) {
              const backgroundURL = processUrl(f.imageUrl)
              return (
                <div key={i} style={styleImage} aria-hidden={true}>
                  <div
                    title={f.title}
                    style={styleFeaturedImage(backgroundURL)}
                    onClick={() => {
                      this.search(f.searchTerm)
                    }}
                    onMouseEnter={() => this.handleMouseOver(i)}
                    onMouseLeave={this.handleMouseOut}
                  />
                </div>
              )
            }
          })}
        </div>
      )

      const flexItems = collapseImage
        ? [ titleList ]
        : [ titleList, imageContainer ]

      return (
        <nav
          aria-labelledby="featuredDatasets"
          style={styleFeaturedDatasetsWrapper}
        >
          <h2 style={styleFeaturedDatasetsLabel} id="featuredDatasets">
            Featured Data Sets
          </h2>
          <div
            ref={featuredRef => {
              this.featuredRef = featuredRef
            }}
            style={styleFeaturedDatasets}
          >
            <FlexRow items={flexItems} />
          </div>
          <br />
          <br />
        </nav>
      )
    }
    else {
      return null
    }
  }
}

export default FeaturedDatasets
