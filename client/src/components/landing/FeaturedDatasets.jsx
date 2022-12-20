import React, {useState, useEffect, useRef} from 'react'
import FlexColumn from '../common/ui/FlexColumn'
import {processUrl} from '../../utils/urlUtils'
import {fontFamilySerif, consolidateStyles} from '../../utils/styleUtils'
import Button from '../common/input/Button'
import ResultGraphic from '../results/ResultGraphic'
import {play_circle_o, pause_circle_o, SvgIcon} from '../common/SvgIcon'
import {SiteColors, boxShadow} from '../../style/defaultStyles'
import DetailGrid from '../collections/detail/DetailGrid'
import {ScreenClassRender} from 'react-grid-system'
import ResizeObserver from 'resize-observer-polyfill' // handle browsers not supporting ResizeObserver

const styleFeaturedDatasetsWrapper = {
  marginTop: '2.618em',
}

const styleFeaturedDatasetsLabel = {
  textAlign: 'center',
  fontFamily: fontFamilySerif(),
  margin: '0 0 0.618em 0',
}

const styleListItem = {
  fontFamily: fontFamilySerif(),
  padding: '1.018em',
  display: 'flex',
}

const styleButtonCorners = (isReversed, screenClass) => {
  if ([ 'xs' ].includes(screenClass)) {
    return {
      borderRadius: '0.309em 0.309em 0 0', // top left and right rounded
      borderTop: '1px solid black',
      borderBottom: '1px solid black',
      borderLeft: '1px solid black', // have to use the same properties as the bigger screen sizes, or unsetting borderLeft: 'none' is difficult
      borderRight: '1px solid black',
    }
  }
  else {
    if (isReversed) {
      // title is on RH side - round right corners
      return {
        borderRadius: '0 0.309em 0.309em 0',
        borderTop: '1px solid black',
        borderBottom: '1px solid black',
        borderRight: '1px solid black',
        borderLeft: 'none',
      }
    }
    else {
      // round left corners
      return {
        borderRadius: '0.309em 0 0 0.309em',
        borderTop: '1px solid black',
        borderBottom: '1px solid black',
        borderLeft: '1px solid black',
        borderRight: 'none',
      }
    }
  }
  return {}
}

const styleButtonWrapper = {
  background: 'transparent',
  height: 'fit-content',
  margin: 0,
}

const styleFeaturedButton = {
  fontFamily: fontFamilySerif(),
  width: '100%',
  height: 'fit-content',
}

const styleFeaturedButtonFocus = {
  textDecoration: 'underline',
  outline: 'none',
  background: '#263f78',
}

const styleFeaturedButtonHover = {
  background: '#263f78',
}

const styleImageCorners = (isReversed, screenClass, isWithoutText) => {
  const borders = {
    borderLeft: ![ 'xs' ].includes(screenClass)
      ? isReversed ? '' : '1px solid black'
      : '',
    borderRight: ![ 'xs' ].includes(screenClass)
      ? isReversed ? '1px solid black' : ''
      : '',
  }
  if ([ 'xs' ].includes(screenClass)) {
    return borders
  }
  else if ([ 'sm', 'md' ].includes(screenClass)) {
    // med and small screen stacks text under image - only add curve to top left or right corner
    if (isReversed) {
      if (isWithoutText) {
        // these are the same for lg and xl too - suggests reversing if layering
        return consolidateStyles(borders, {
          borderRadius: '0.309em 0 0.309em 0.309em',
        })
      }
      return consolidateStyles(borders, {borderRadius: '0.309em 0 0 0'})
    }
    else {
      if (isWithoutText) {
        return consolidateStyles(borders, {
          borderRadius: '0 0.309em 0.309em 0.309em',
        })
      }
      return consolidateStyles(borders, {borderRadius: '0 0.309em 0 0'})
    }
  }
  else {
    // large screen sizes place text next to image, curve the opposite bottom corner
    if (isReversed) {
      if (isWithoutText) {
        return consolidateStyles(borders, {
          borderRadius: '0.309em 0 0.309em 0.309em',
        })
      }
      return consolidateStyles(borders, {borderRadius: '0 0 0.309em 0'})
    }
    else {
      if (isWithoutText) {
        return consolidateStyles(borders, {
          borderRadius: '0 0.309em 0.309em 0.309em',
        })
      }
      return consolidateStyles(borders, {borderRadius: '0 0 0 0.309em'})
    }
  }

  return borders
}

const styleImageWrapper = {
  boxShadow: boxShadow,
  background: '#F9F9F9',
  height: '100%',
}

const styleDescriptionCorners = (isReversed, screenClass) => {
  return {
    borderLeft: [ 'sm', 'md' ].includes(screenClass)
      ? isReversed ? '' : '1px solid black'
      : '',
    borderRight: [ 'sm', 'md' ].includes(screenClass)
      ? isReversed ? '1px solid black' : ''
      : '',
    borderRadius: [ 'xs', 'sm', 'md' ].includes(screenClass)
      ? '0 0 0.309em 0.309em'
      : isReversed ? '0.309em 0 0 0.309em' : '0 0.309em 0.309em 0',
  }
}

const styleDescriptionWrapper = {
  boxShadow: boxShadow,
  background: '#F9F9F9',
  display: 'flex',
  justifyContent: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
}

const styleDescription = {
  margin: '1.618em',
  fontSize: '1.1em',
}

const styleImage = {
  width: '100%',
  objectFit: 'cover',
  cursor: 'pointer',
  border: '1px solid black',
}

const FeaturedDatasetH3Button = ({styles, title, searchClick}) => {
  return (
    <h3 style={styleButtonWrapper}>
      <Button
        role="link"
        text={title}
        title={`${title} Featured Data Search`}
        onClick={searchClick}
        style={consolidateStyles(styleFeaturedButton, styles)}
        styleHover={styleFeaturedButtonHover}
        styleFocus={styleFeaturedButtonFocus}
      />
    </h3>
  )
}

const FeaturedDatasetImage = ({
  styles,
  url,
  caption,
  searchClick,
  imageRef,
}) => {
  const backgroundURL = processUrl(url)

  return (
    <div style={consolidateStyles(styleImageWrapper, styles)}>
      <FlexColumn
        style={{
          justifyContent: 'center',
          height: '100%',
        }}
        items={[
          <p key="centerMe" style={{padding: '1.618em', margin: 0}}>
            <figure ref={imageRef}>
              <img
                onClick={searchClick}
                style={styleImage}
                src={backgroundURL}
                alt=""
                aria-hidden="true"
              />
              <figcaption style={{textAlign: 'center'}}>{caption}</figcaption>
            </figure>
          </p>,
        ]}
      />
    </div>
  )
}

const FeaturedDatasetDescription = ({
  styles,
  title,
  description,
  maxHeight,
  needsShowButton,
  showDescription,
  showDescriptionFunc,
  hideDescriptionFunc,
}) => {
  const styleDescriptionMask = {
    position: 'absolute',
    width: '100%',
    height: '100%',
    background:
      showDescription || !needsShowButton
        ? '#00000000'
        : 'linear-gradient(#F9F9F900 80%, #F9F9F9F9)',
  }
  const styleExpandDescriptionButton = {
    float: 'right',
    textDecoration: 'underline',
    border: 'none',
    margin: '.618em',
    background: 'none',
  }
  return (
    <div style={consolidateStyles(styleDescriptionWrapper, styles)}>
      <FlexColumn
        style={consolidateStyles(styleDescription, {
          minHeight: '3em',
          maxHeight:
            showDescription || !needsShowButton ? '' : `${maxHeight}px`,
        })}
        items={[
          <div
            key="desc"
            style={{overflow: 'hidden', position: 'relative', flex: 1}}
          >
            <div style={styleDescriptionMask} />
            {description}
          </div>,
          needsShowButton ? (
            <button
              style={styleExpandDescriptionButton}
              onClick={
                showDescription ? hideDescriptionFunc : showDescriptionFunc
              }
              title={
                showDescription ? (
                  `collapse ${title} description`
                ) : (
                  `expand ${title} description`
                )
              }
            >
              {showDescription ? 'collapse' : 'expand'}
            </button>
          ) : null,
        ]}
      />
    </div>
  )
}

const FeaturedDatasetRow = ({
  description,
  searchTerm,
  search,
  title,
  imageUrl,
  caption,
  isReversed,
  screenClass,
}) => {
  const searchClick = () => search(searchTerm)
  const [ showDescription, setShowDescription ] = useState(false)
  const imageRef = useRef()
  const [ imageHeight, setImageHeight ] = useState(0)
  const [ resizeObserver ] = useState(() => {
    return new ResizeObserver((entries, observer) => {
      if (!showDescription) {
        setImageHeight(imageRef.current.getBoundingClientRect().height)
      }
    })
  })

  // observe resize changes to the content
  useEffect(() => {
    if (imageRef.current) {
      // set image size right away for fix browsers like Safari, that don't get the events on the first load
      setImageHeight(imageRef.current.getBoundingClientRect().height)
      resizeObserver.observe(imageRef.current)
    }
    return () => resizeObserver.disconnect()
  }, [])

  // layout for image only, no description grid
  const titleGridWithNoDescription = isReversed
    ? {
        sm: 4,
        md: 3,
        lg: 2,
        push: {sm: 8, md: 9, lg: 10},
        style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 5},
      }
    : {sm: 4, md: 3, lg: 2}
  const imageGridWithNoDescription = isReversed
    ? {
        sm: 8,
        md: 9,
        lg: 10,
        pull: {sm: 4, md: 3, lg: 2},
        style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 3},
      }
    : {sm: 8, md: 9, lg: 10}

  // layout for image plus description grid
  // grid totals must add up to 12 for all the columns
  const titleGrid = isReversed
    ? {
        sm: 4,
        md: 3,
        lg: 2,
        xl: 2,
        push: {sm: 8, md: 9, lg: 10},
        style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 5},
      }
    : {sm: 4, md: 3, lg: 2}
  const imageGrid = isReversed
    ? {
        sm: 8,
        md: 9,
        lg: showDescription ? 4 : 6,
        xl: showDescription ? 3 : 7,
        pull: {sm: 4, md: 3},
        style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 3},
        push: {lg: showDescription ? 4 : 2, xl: showDescription ? 5 : 1},
      }
    : {sm: 8, md: 9, lg: showDescription ? 4 : 6, xl: showDescription ? 3 : 7}
  const descGrid = isReversed
    ? {
        sm: 8,
        md: 9,
        lg: showDescription ? 6 : 4,
        xl: showDescription ? 7 : 3,
        pull: {
          sm: 4,
          md: 3,
          lg: showDescription ? 6 : 8,
          xl: showDescription ? 5 : 9,
        },
      }
    : {sm: 8, md: 9, lg: showDescription ? 6 : 4, xl: showDescription ? 7 : 3}

  const isWithoutDescription = description == null || _.isEmpty(description)
  const titleComponent = (
    <FeaturedDatasetH3Button
      styles={styleButtonCorners(isReversed, screenClass)}
      title={title}
      searchClick={searchClick}
    />
  )
  const imageComponent = (
    <FeaturedDatasetImage
      imageRef={imageRef}
      styles={styleImageCorners(isReversed, screenClass, isWithoutDescription)}
      url={imageUrl}
      caption={caption}
      searchClick={searchClick}
    />
  )
  const descriptionComponent = (
    <FeaturedDatasetDescription
      title={title}
      maxHeight={imageHeight}
      needsShowButton={[ 'lg', 'xl' ].includes(screenClass)}
      showDescription={showDescription}
      showDescriptionFunc={() => {
        resizeObserver.unobserve(imageRef.current)
        setShowDescription(true)
      }}
      hideDescriptionFunc={() => {
        resizeObserver.observe(imageRef.current)
        setShowDescription(false)
      }}
      styles={styleDescriptionCorners(isReversed, screenClass)}
      description={description}
    />
  )

  if (isWithoutDescription) {
    return (
      <DetailGrid
        grid={[ [ titleComponent, imageComponent ] ]}
        colWidths={[ titleGridWithNoDescription, imageGridWithNoDescription ]}
      />
    )
  }
  return (
    <DetailGrid
      grid={[ [ titleComponent, imageComponent, descriptionComponent ] ]}
      colWidths={[ titleGrid, imageGrid, descGrid ]}
    />
  )
}

const FeaturedDatasets = props => {
  const search = query => {
    const {submit} = props
    submit(query)
  }

  if (props.featured !== null && props.featured.length > 0) {
    const featuredList = props.featured.map((f, i, arr) => {
      const isReversed = i % 2 == 1
      return (
        <li style={styleListItem} key={i}>
          <ScreenClassRender
            render={screenClass => {
              return (
                <FeaturedDatasetRow
                  screenClass={screenClass}
                  description={f.description}
                  searchTerm={f.searchTerm}
                  search={search}
                  title={f.title}
                  imageUrl={f.imageUrl}
                  caption={f.caption}
                  isReversed={isReversed}
                />
              )
            }}
          />
        </li>
      )
    })
    const featuredOList = <ol>{featuredList}</ol>

    return (
      <nav
        aria-labelledby="featuredDatasets"
        style={styleFeaturedDatasetsWrapper}
      >
        <h2 style={styleFeaturedDatasetsLabel} id="featuredDatasets">
          Featured Data Sets
        </h2>
        <FlexColumn items={[ featuredOList ]} />
      </nav>
    )
  }
  else {
    return null
  }
}

export default FeaturedDatasets
