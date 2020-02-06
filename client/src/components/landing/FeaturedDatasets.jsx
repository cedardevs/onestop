import React, {useEffect, useState, useRef} from 'react'
import FlexRow from '../common/ui/FlexRow'
import FlexColumn from '../common/ui/FlexColumn'
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

const styleTitle = (first, last, collapseImage) => {
  const topRightRadius = collapseImage ? '1em' : '0'
  const bottomRightRadius = collapseImage ? '1em' : '0'
  return {
    background: '#026dab',
    // textAlign: collapseImage ? 'center' : 'right',
    fontFamily: fontFamilySerif(),
    padding: '.609em 1.018em',
    // borderRadius: first
    //   ? `1em ${topRightRadius} 0 0`
    //   : last ? `0 0 ${bottomRightRadius} 1em` : 'none',
    display: 'flex',
    // justifyContent: collapseImage ? 'center' : 'flex-end',
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
  // display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  flex: 2,
  // borderRadius: '0 1em 1em 1em',
  // background: '#263f78',
  cursor: 'pointer',
  // width: '66%',
  height: '20em',
  // padding: '1em',
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
    flex: 1,
    textDecoration: 'underline',
    fontFamily: fontFamilySerif(),
    fontSize: '1.25em',
    width: '100%',
    background: 'transparent',
    color: 'inherit',
    textAlign: collapseImage ? 'center' : 'right',
    justifyContent: collapseImage ? 'center' : 'flex-end',
    padding: '.309em',
    margin: '.309em 0',
    marginRight: '1.018em',
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

const FeaturedDatasets = props => {
  const [ current, setCurrent ] = useState(0)
  const [ carouselLength, setCarouselLength ] = useState(0)
  const [ collapseImage, setCollapseImage ] = useState(false)
  const [ hovering, setHovering ] = useState(false)
  const [ focusing, setFocusing ] = useState(false)
  const [ focusingCurrent, setFocusingCurrent ] = useState()
  const featuredRef = useRef()

  const search = query => {
    const {submit} = props
    submit(query)
  }

  const handleBlur = () => {
    setFocusing(false)
    setFocusingCurrent(null)
  }

  const handleFocus = i => {
    setFocusing(true)
    setFocusingCurrent(i)
    setCurrent(i)
  }

  const handleMouseOut = () => {
    setHovering(false)
    setCurrent(focusing ? focusingCurrent : current)
  }

  const handleMouseOver = i => {
    setHovering(true)
    setCurrent(i)
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

  if (props.featured !== null && props.featured.length > 0) {
    const titleList = //(
      // <ul key="title-list" style={styleTitleList(collapseImage)}>
      props.featured.map((f, i, arr) => {
        const first = i === 0
        // const last = i === arr.length - 1
        const last = false // TODO nuke this then?
        const backgroundURL = processUrl(f.imageUrl)

        return (
          <li
            style={styleTitle(first, last, collapseImage)}
            key={i}
            onMouseEnter={() => handleMouseOver(i)}
            onMouseLeave={handleMouseOut}
          >
            <FlexRow
              style={{width: '100%'}}
              items={[
                <Button
                  key="button"
                  text={f.title}
                  title={`${f.title} Featured Data Search`}
                  onClick={() => search(f.searchTerm)}
                  onFocus={() => handleFocus(i)}
                  onBlur={handleBlur}
                  style={styleFeaturedButton(collapseImage)}
                  styleHover={styleFeaturedButtonHover}
                  styleFocus={styleFeaturedButtonFocus}
                />,
                <div key="image" style={styleImageContainer}>
                  <div style={styleImage} aria-hidden={true}>
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
                </div>,
                <div style={{flex: 2, marginLeft: '1.018em'}} key="description">
                  {f.description}
                </div>,
              ]}
            />
          </li>
        )
      }) //}
    // </ul>
    // )

    const flexItems = [ titleList ]

    return (
      <nav
        aria-labelledby="featuredDatasets"
        style={styleFeaturedDatasetsWrapper}
      >
        <h2 style={styleFeaturedDatasetsLabel} id="featuredDatasets">
          Featured Data Sets
        </h2>
        <ul ref={featuredRef} style={styleFeaturedDatasets}>
          <FlexColumn items={flexItems} />
        </ul>
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
