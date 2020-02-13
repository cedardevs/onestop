import React from 'react'
import FlexRow from '../common/ui/FlexRow'
import FlexColumn from '../common/ui/FlexColumn'
import {processUrl} from '../../utils/urlUtils'
import {fontFamilySerif, consolidateStyles} from '../../utils/styleUtils'
import Button from '../common/input/Button'
import ResultGraphic from '../results/ResultGraphic'
import {play_circle_o, pause_circle_o, SvgIcon} from '../common/SvgIcon'
import {SiteColors, boxShadow} from '../../style/defaultStyles'
import DetailGrid from '../collections/detail/DetailGrid'
import {ScreenClassRender} from 'react-grid-system'

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
    }
  }
  else {
    if (isReversed) {
      // title is on RH side - round right corners
      return {borderRadius: '0 0.309em 0.309em 0'}
    }
    else {
      // round left corners
      return {borderRadius: '0.309em 0 0 0.309em'}
    }
  }
  return {}
}

const styleButtonWrapper = {
  borderBottom: '1px solid black',
  boxShadow: boxShadow,
  background: '#F9F9F9',
  height: 'fit-content',
  margin: 0,
}

const styleFeaturedButton = {
  textDecoration: 'underline',
  fontFamily: fontFamilySerif(),
  fontSize: '1.25em',
  width: '100%',
  height: 'fit-content',
  background: 'transparent',
  color: SiteColors.LINK,
  padding: '.309em',
  display: 'block',
}

const styleFeaturedButtonFocus = {
  textDecoration: 'underline',
  outline: '2px dashed #5C87AC',
  background: 'transparent',
}

const styleFeaturedButtonHover = {
  background: 'transparent',
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
  // overflowY: 'hidden',
}

const styleDescription = {
  padding: '1.618em',
  margin: 0,
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
    <h3 key="button" style={consolidateStyles(styleButtonWrapper, styles)}>
      <Button
        role="link"
        text={title}
        title={`${title} Featured Data Search`}
        onClick={searchClick}
        style={styleFeaturedButton}
        styleHover={styleFeaturedButtonHover}
        styleFocus={styleFeaturedButtonFocus}
      />
    </h3>
  )
}

const FeaturedDatasetImage = ({styles, url, searchClick}) => {
  const backgroundURL = processUrl(url)
  return (
    <div key="img" style={consolidateStyles(styleImageWrapper, styles)}>
      <p style={{padding: '1.618em', margin: 0}}>
        <img
          onClick={searchClick}
          style={styleImage}
          src={backgroundURL}
          alt=""
          aria-hidden="true"
        />
      </p>
    </div>
  )
}

const FeaturedDatasetDescription = ({styles, description}) => {
  return (
    <div key="desc" style={consolidateStyles(styleDescriptionWrapper, styles)}>
      <p style={styleDescription}>{description}</p>
    </div>
  )
}

const FeaturedDatasetRow = ({
  description,
  searchTerm,
  search,
  title,
  imageUrl,
  isReversed,
  screenClass,
}) => {
  const searchClick = () => search(searchTerm)

  // if (description == null || _.isEmpty(description)) {
  //   return (
  //     <ScreenClassRender
  //       render={screenClass => {
  //         // grid totals must add up to 12 for all the columns
  //         // const titleGrid = isReversed
  //         //   ? {
  //         //       sm: 4,
  //         //       md: 3,
  //         //       lg: 2,
  //         //       push: {sm: 8, md: 9, lg: 10},
  //         //       style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 5},
  //         //     }
  //         //   : {sm: 4, md: 3, lg: 2}
  //         // const imageGrid = isReversed
  //         //   ? {
  //         //       sm: 8,
  //         //       md: 9,
  //         //       lg: 10,
  //         //       pull: {sm: 4, md: 3, lg: 2},
  //         //       style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 3},
  //         //     }
  //         //   : {sm: 8, md: 9, lg: 10}
  //
  //         return (
  //           <DetailGrid
  //             grid={[
  //               [
  //                 titleComponent, imageComponent
  //               ],
  //             ]}
  //             colWidths={[ titleGridWithNoDescription, imageGridWithNoDescription ]}
  //           />
  //         )
  //       }}
  //     />
  //   )
  // }

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
        lg: 6,
        xl: 7,
        pull: {sm: 4, md: 3},
        style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 3},
        push: {lg: 2, xl: 1},
      }
    : {sm: 8, md: 9, lg: 6, xl: 7}
  const descGrid = isReversed
    ? {sm: 8, md: 9, lg: 4, xl: 3, pull: {sm: 4, md: 3, lg: 8, xl: 9}}
    : {sm: 8, md: 9, lg: 4, xl: 3}

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
      styles={styleImageCorners(isReversed, screenClass, isWithoutDescription)}
      url={imageUrl}
      searchClick={searchClick}
    />
  )
  const descriptionComponent = (
    <FeaturedDatasetDescription
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
                  isReversed={isReversed}
                />
              )
            }}
          />
        </li>
      )
    })

    return (
      <nav
        aria-labelledby="featuredDatasets"
        style={styleFeaturedDatasetsWrapper}
      >
        <h2 style={styleFeaturedDatasetsLabel} id="featuredDatasets">
          Featured Data Sets
        </h2>

        <ul>
          <FlexColumn items={[ featuredList ]} />
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
