import React from 'react'
import FlexRow from '../common/ui/FlexRow'
import FlexColumn from '../common/ui/FlexColumn'
import {processUrl} from '../../utils/urlUtils'
import {fontFamilySerif} from '../../utils/styleUtils'
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

const styleButtonWrapper = (isReversed, screenClass) => { return {
  borderRadius: [ 'xs' ].includes(screenClass) ? '0.309em 0.309em 0 0': isReversed ? '0 0.309em 0.309em 0': '0.309em 0 0 0.309em',
  borderBottom: '1px solid black',
  boxShadow: boxShadow,
  background: '#F9F9F9',
  height: 'fit-content',
      margin: 0,
}}

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

const styleImageWrapper = (isReversed, screenClass) => { // TODO need more styling for image wrapper borderRadius when there is NO description!!
  return {
    // xs layout is vertically stacked, needs no left or right border
    borderLeft: ![ 'xs' ].includes(screenClass)
      ? isReversed ? '' : '1px solid black'
      : '',
    borderRight: ![ 'xs' ].includes(screenClass)
      ? isReversed ? '1px solid black' : ''
      : '',
              borderRadius: [ 'xs' ].includes(screenClass) ? '' : [ 'sm', 'md' ].includes(screenClass) ? isReversed? '0.309em 0 0 0': '0 0.309em 0 0': isReversed ? '0 0 0.309em 0' : '0 0 0 0.309em',
    boxShadow: boxShadow,
    background: '#F9F9F9',
    height: '100%',
    // margin: 0,

  }
}

  const styleDescriptionWrapper = (isReversed, screenClass) => {
    return {
  boxShadow: boxShadow,
    borderLeft: [ 'sm', 'md' ].includes(screenClass)
      ? isReversed ? '' : '1px solid black'
      : '',
    borderRight: [ 'sm', 'md' ].includes(screenClass)
      ? isReversed ? '1px solid black' : ''
      : '',

        borderRadius: [ 'xs', 'sm', 'md' ].includes(screenClass) ? '0 0 0.309em 0.309em': isReversed ? '0.309em 0 0 0.309em' : '0 0.309em 0.309em 0',
  background: '#F9F9F9',
  display: 'flex',
  justifyContent: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
  // overflowY: 'hidden',
}
}

const styleDescription = {
  padding: '1.618em',
  margin: 0,
  fontSize: '1.1em',
}

const styleImage = {
  // margin: '0 1.618em 1.618em 0',
  width: '100%',
  objectFit: 'cover',
    cursor: 'pointer',
  border: '1px solid black',
}

const FeaturedDatasets = props => {
  const search = query => {
    const {submit} = props
    submit(query)
  }

  if (props.featured !== null && props.featured.length > 0) {
    const featuredList = props.featured.map((f, i, arr) => {
      const backgroundURL = processUrl(f.imageUrl)
      const isReversed = i % 2 == 1
      const collectionImage = <img onClick={() => {
          search(f.searchTerm)
        }} style={styleImage} src={backgroundURL} alt="" aria-hidden="true" />

      if(f.description == null || _.isEmpty(f.description)) {
        return (
          <ScreenClassRender
            render={screenClass => {
              // grid totals must add up to 12 for all the columns
              const titleGrid = isReversed
                ? {
                    sm: 4,
                    md: 3,
                    lg: 2,
                    push: {sm: 8, md: 9, lg: 10},
                    style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 5},
                  }
                : {sm: 4, md: 3, lg: 2}
              const imageGrid = isReversed
                ? {
                    sm: 8,
                    md: 9,
                    lg: 10,
                    pull: {sm: 4, md: 3, lg: 2},
                    style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 3},
                    // push: {lg: 1},
                  }
                : {sm: 8, md: 9, lg: 10}
              return (
                <li style={styleListItem} key={i}>
                  <DetailGrid
                    grid={[
                      [
                        <h3 key="button" style={styleButtonWrapper(isReversed, screenClass)}>
                          <Button
                            role="link"
                            text={f.title}
                            title={`${f.title} Featured Data Search`}
                            onClick={() => search(f.searchTerm)}
                            style={styleFeaturedButton}
                            styleHover={styleFeaturedButtonHover}
                            styleFocus={styleFeaturedButtonFocus}
                          />
                      </h3>,
                        <div
                          key="img"
                          style={styleImageWrapper(isReversed, screenClass)}
                        >
                        <p style={{padding: '1.618em',margin:0}}>
                          {collectionImage}</p>
                        </div>,
                      ],
                    ]}
                    colWidths={[ titleGrid, imageGrid ]}
                  />
                </li>
              )
            }}
          />
        )
      }
      return (
        <ScreenClassRender
          render={screenClass => {
            // grid totals must add up to 12 for all the columns
            const titleGrid = isReversed
              ? {
                  sm: 4,
                  md: 3,
                  lg: 2,
                  push: {sm: 8, md: 9, lg: 10},
                  style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 5},
                }
              : {sm: 4, md: 3, lg: 2}
            const imageGrid = isReversed
              ? {
                  sm: 8,
                  md: 9,
                  lg: 7,
                  pull: {sm: 4, md: 3},
                  style: {zIndex: [ 'xs' ].includes(screenClass) ? 1 : 3},
                  push: {lg: 1},
                }
              : {sm: 8, md: 9, lg: 7}
            const descGrid = isReversed
              ? {sm: 8, md: 9, lg: 3, pull: {sm: 4, md: 3, lg: 9}}
              : {sm: 8, md: 9, lg: 3}
            return (
              <li style={styleListItem} key={i}>
                <DetailGrid
                  grid={[
                    [
                      <h3 key="button" style={styleButtonWrapper(isReversed, screenClass)}>
                        <Button
                          role="link"
                          text={f.title}
                          title={`${f.title} Featured Data Search`}
                          onClick={() => search(f.searchTerm)}
                          style={styleFeaturedButton}
                          styleHover={styleFeaturedButtonHover}
                          styleFocus={styleFeaturedButtonFocus}
                        />
                    </h3>,
                      <div
                        key="img"
                        style={styleImageWrapper(isReversed, screenClass)}
                      >
                      <p style={{padding: '1.618em',margin:0}}>
                        {collectionImage}</p>
                      </div>,
                      <div key="desc" style={styleDescriptionWrapper(isReversed, screenClass)}>
                        <p style={styleDescription}>{f.description}</p>
                      </div>,
                    ],
                  ]}
                  colWidths={[ titleGrid, imageGrid, descGrid ]}
                />
              </li>
            )
          }}
        />
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
