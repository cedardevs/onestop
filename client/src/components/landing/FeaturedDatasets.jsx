import React, {useEffect, useState, useRef} from 'react'
import FlexRow from '../common/ui/FlexRow'
import {processUrl} from '../../utils/urlUtils'
import {fontFamilySerif} from '../../utils/styleUtils'
import Button from '../common/input/Button'
import {play_circle_o, pause_circle_o, SvgIcon} from '../common/SvgIcon'

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
    padding: '.609em 1.018em',
    borderRadius: first
      ? `1em ${topRightRadius} 0 0`
      : last ? `0 0 ${bottomRightRadius} 1em` : 'none',
    display: 'flex',
    justifyContent: collapseImage ? 'center' : 'flex-end',
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

const styleFeaturedButton = collapseImage => {
  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    width: '100%',
    background: 'transparent',
    color: 'inherit',
    textAlign: collapseImage ? 'center' : 'right',
    justifyContent: collapseImage ? 'center' : 'flex-end',
    padding: '.309em',
    margin: '.309em 0',
  }
}

const styleFeaturedButtonFocus = {
  textDecoration: 'underline',
  background: 'transparent',
}

const styleFeaturedButtonHover = {
  textDecoration: 'underline',
  background: 'transparent',
}

const stylePlayPauseButton = collapseImage => {
  return {
    fontFamily: fontFamilySerif(),
    fontSize: '1em',
    background: 'transparent',
    color: 'inherit',
    textAlign: collapseImage ? 'center' : 'right',
    justifyContent: collapseImage ? 'center' : 'flex-end',
    padding: '.309em',
    margin: '.309em 0',
    fill: 'white',
  }
}

const stylePlayPauseFocus = {
  outline: '2px dashed white',
  background: 'transparent',
}

const stylePlayPauseHover = {
  outline: '2px dashed white',
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

const FeaturedDatasets = props => {
  const [ current, setCurrent ] = useState(0)
  const [ carouselLength, setCarouselLength ] = useState(0)
  const [ timer, setTimer ] = useState()
  const [ collapseImage, setCollapseImage ] = useState(false)
  const [ hovering, setHovering ] = useState(false)
  const [ focusing, setFocusing ] = useState(false)
  const [ focusingCurrent, setFocusingCurrent ] = useState()
  const [ manualPause, setManualPause ] = useState(false)
  const featuredRef = useRef()

  const isPaused = () => {
    return !focusing && !hovering && !manualPause
  }

  const search = query => {
    const {submit} = props
    submit(query)
  }

  const togglePause = () => {
    if (isPaused()) {
      timer.resume()
    }
  }

  const toggleManualPause = () => {
    timer.pause()
    setManualPause(!manualPause)
    togglePause()
  }

  const handleBlur = () => {
    setFocusing(false)
    setFocusingCurrent(null)
    togglePause()
  }

  const handleFocus = i => {
    timer.pause()
    setFocusing(true)
    setFocusingCurrent(i)
    setCurrent(i)
    togglePause()
  }

  const handleMouseOut = () => {
    setHovering(false)
    setCurrent(focusing ? focusingCurrent : current)
    togglePause()
  }

  const handleMouseOver = i => {
    timer.pause()
    setHovering(true)
    setCurrent(i)
    togglePause()
  }

  const setupTimer = items => {
    if (items && (items.length || carouselLength !== items.length)) {
      setCarouselLength(items.length)

      if (!timer) {
        setTimerState()
      }
    }
  }

  const setTimerState = () => {
    setTimer(new Timer(() => rotateCarousel(), 5000))
  }

  const rotateCarousel = () => {
    if (isPaused()) {
      const newCurrent = (current + 1) % carouselLength
      setCurrent(newCurrent)
    }
    setTimerState()
  }

  useEffect(
    // will recieve props
    () => {
      setupTimer(props.featured)
    },
    [ props.featured ]
  )

  useEffect(() => {
    // did mount:
    setupTimer(props.featured)
    debounceResize()
    window.addEventListener('resize', debounceResize)

    // unmount:
    return function cleanup(){
      if (timer) {
        timer.pause()
      }
      window.removeEventListener('resize', debounceResize)
    }
  })

  let resizeId // ???? TODO hook version of this ok?
  const debounceResize = () => {
    // prevent resize work unless threshold is reached
    // this helps ensure the new width doesn't stick on a transient value
    const resizeThreshold = 250 // ms
    clearTimeout(resizeId)
    resizeId = setTimeout(resize, resizeThreshold)
  }

  const resize = () => {
    // do work only if iframe exists
    if (featuredRef) {
      // get new width dynamically
      const featuredRect = featuredRef.current.getBoundingClientRect()
      const newWidth = parseFloat(featuredRect.width)
      // if the image's new width goes below our threshold, collapse
      setCollapseImage(newWidth < 700)
    }
  }

  // render:
  const manualPauseLabel = !manualPause ? 'Pause Animation' : 'Start Animation'
  const manualPauseAriaLabel = !manualPause
    ? 'Pause Featured Datasets Animation'
    : 'Start Featured Datasets Animation'
  const manualPauseButton = (
    <Button
      title={manualPauseAriaLabel}
      text={manualPauseLabel}
      onClick={toggleManualPause}
      style={stylePlayPauseButton(collapseImage)}
      styleFocus={stylePlayPauseFocus}
      styleHover={stylePlayPauseHover}
      ariaSelected={!isPaused()}
    >
      <SvgIcon
        size="1.3em"
        path={!manualPause ? pause_circle_o : play_circle_o}
      />{' '}
      <span>{manualPauseLabel}</span>
    </Button>
  )

  if (props.featured !== null && props.featured.length > 0) {
    const titleList = (
      <ul key="title-list" style={styleTitleList(collapseImage)}>
        {props.featured.map((f, i, arr) => {
          const active = current === i
          const first = i === 0
          // const last = i === arr.length - 1
          const last = false
          return (
            <li
              style={styleTitle(active, first, last, collapseImage)}
              key={i}
              onMouseEnter={() => handleMouseOver(i)}
              onMouseLeave={handleMouseOut}
            >
              <Button
                text={f.title}
                title={`${f.title} Featured Data Search`}
                onClick={() => search(f.searchTerm)}
                onFocus={() => handleFocus(i)}
                onBlur={handleBlur}
                style={styleFeaturedButton(collapseImage)}
                styleHover={styleFeaturedButtonHover}
                styleFocus={styleFeaturedButtonFocus}
              />
            </li>
          )
        })}
        <li style={styleTitle(false, false, true, collapseImage)}>
          {manualPauseButton}
        </li>
      </ul>
    )

    const imageContainer = (
      <div key="image-container" style={styleImageContainer}>
        {props.featured.map((f, i) => {
          const active = current === i
          if (active) {
            const backgroundURL = processUrl(f.imageUrl)
            return (
              <div key={i} style={styleImage} aria-hidden={true}>
                <div
                  title={f.title}
                  style={styleFeaturedImage(backgroundURL)}
                  onClick={() => {
                    search(f.searchTerm)
                  }}
                  onMouseEnter={() => handleMouseOver(i)}
                  onMouseLeave={handleMouseOut}
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
        <div ref={featuredRef} style={styleFeaturedDatasets}>
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

export default FeaturedDatasets
