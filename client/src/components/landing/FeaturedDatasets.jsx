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
import {Visible} from 'react-grid-system'
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

const styleButtonWrapper = {
  // width: '100%',
  borderBottom: '1px solid black',

  boxShadow: boxShadow,
  background: '#F9F9F9',
  // flex: 1,
  height: 'fit-content',
  // justifyContent: 'flex-end',
}

const styleFeaturedButton = {
  textDecoration: 'underline',
  fontFamily: fontFamilySerif(),
  fontSize: '1.25em',
  width: '100%',
  height: 'fit-content',
  background: 'transparent',
  // color: 'inherit',
  color: SiteColors.LINK,
  // textAlign: isReversed ? 'left' : 'right',
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

const styleImageWrapper = (isReversed, screenClass) => {
  return {
    borderLeft: ![ 'xs' ].includes(screenClass)
      ? isReversed ? '' : '1px solid black'
      : '',
    borderRight: ![ 'xs' ].includes(screenClass)
      ? isReversed ? '1px solid black' : ''
      : '',
    boxShadow: boxShadow,
    background: '#F9F9F9',
    // flex: 3,
    // display: 'flex',
    // justifyContent: 'center',
    // alignSelf: 'stretch',
    height: '100%',
    cursor: 'pointer',
    margin: 0,
  }
}

const styleDescriptionWrapper = {
  // borderLeft: isReversed? '':'1px solid black',
  // borderRight: isReversed? '1px solid black':'',
  boxShadow: boxShadow,
  background: '#F9F9F9',
  // flex: 3,
  display: 'flex',
  justifyContent: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
}

// const styleImageWrapper = {
//
//   padding: '0.618em 1.618em 1.618em 1.618em',
//   margin: 0,
//   fontSize: '1.1em',
//     display: 'flex',
//     justifyContent: 'center',
//     alignSelf: 'stretch',
// }
const styleDescription = {
  // width: '100%',
  padding: '0.618em 1.618em 1.618em 1.618em',
  margin: 0,
  fontSize: '1.1em',
  // display: 'flex',
  // justifyContent: 'center',
  // alignSelf: 'stretch',
}

const styleImage = {
  // float: 'left',
  // alignSelf: 'flex-start',
  margin: '0 1.618em 1.618em 0',
  // width: '32%',
  // width: '60%',
  width: '100%',
  // minWidth: '15em',
  objectFit: 'cover',
  border: '1px solid black',
}

const FeaturedDatasets = props => {
  const search = query => {
    const {submit} = props
    submit(query)
  }

  const renderCollectionImage = thumbnail => {
    const imgUrl = processUrl(thumbnail)
    return <img style={styleImage} src={imgUrl} alt="" aria-hidden="true" />
  }

  if (props.featured !== null && props.featured.length > 0) {
    const featuredList = props.featured.map((f, i, arr) => {
      const backgroundURL = processUrl(f.imageUrl)
      const isReversed = i % 2 == 1
      const collectionImage = renderCollectionImage(backgroundURL)

      // TODO pull out embedded styles!
      /*
      <FlexRow
        style={{flexDirection: isReversed ? 'row-reverse' : 'row'}}
        items={[

        ]}
      />
      */
      return (
        <ScreenClassRender
          render={screenClass => {
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
                      <div key="button" style={styleButtonWrapper}>
                        <Button
                          text={f.title}
                          title={`${f.title} Featured Data Search`}
                          onClick={() => search(f.searchTerm)}
                          style={styleFeaturedButton}
                          styleHover={styleFeaturedButtonHover}
                          styleFocus={styleFeaturedButtonFocus}
                        />
                      </div>,
                      <div
                        key="img"
                        style={styleImageWrapper(isReversed, screenClass)}
                        onClick={() => {
                          search(f.searchTerm)
                        }}
                      >
                        <p style={styleDescription}>{collectionImage}</p>
                      </div>,
                      <div key="desc" style={styleDescriptionWrapper}>
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
        <p>
          <span>Your current screen class is </span>
          <Visible xs>
            <strong>xs</strong>
          </Visible>
          <Visible sm>
            <strong>sm</strong>
          </Visible>
          <Visible md>
            <strong>md</strong>
          </Visible>
          <Visible lg>
            <strong>lg</strong>
          </Visible>
          <Visible xl>
            <strong>xl</strong>
          </Visible>
          <span>.</span>
        </p>

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
