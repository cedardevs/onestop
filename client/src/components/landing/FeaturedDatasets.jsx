import React from 'react'
import FlexRow from '../common/ui/FlexRow'
import FlexColumn from '../common/ui/FlexColumn'
import {processUrl} from '../../utils/urlUtils'
import {fontFamilySerif} from '../../utils/styleUtils'
import Button from '../common/input/Button'
import ResultGraphic from '../results/ResultGraphic'
import {play_circle_o, pause_circle_o, SvgIcon} from '../common/SvgIcon'

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

const styleFeaturedButton = isReversed => {
  return {
    textDecoration: 'underline',
    fontFamily: fontFamilySerif(),
    fontSize: '1.25em',
    width: '100%',
    height: 'fit-content',
    background: 'transparent',
    color: 'inherit',
    textAlign: isReversed ? 'left' : 'right',
    padding: '.309em',
    display: 'block',
  }
}

const styleFeaturedButtonFocus = {
  textDecoration: 'underline',
  outline: '2px dashed #5C87AC',
  background: 'transparent',
}

const styleFeaturedButtonHover = {
  background: 'transparent',
}

const styleDescriptionWrapper = {
  border: '2px solid black',
  background: '#F9F9F9',
  flex: 3,
  display: 'flex',
  justifyContent: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
}

const styleDescription = {
  width: '100%',
  padding: '0.618em 1.618em 1.618em 1.618em',
  margin: 0,
  fontSize: '1.1em',
}

const styleImage = {
  float: 'left',
  alignSelf: 'flex-start',
  margin: '0 1.618em 1.618em 0',
  // width: '32%',
  width: '60%',
  minWidth: '15em',
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

      return (
        <li style={styleListItem} key={i}>
          <FlexRow
            style={{flexDirection: isReversed ? 'row-reverse' : 'row'}}
            items={[
              <div
                key="button"
                style={{
                  width: '100%',
                  borderBottom: '2px solid black',
                  flex: 1,
                  height: 'fit-content',
                  justifyContent: 'flex-end',
                }}
              >
                <Button
                  text={f.title}
                  title={`${f.title} Featured Data Search`}
                  onClick={() => search(f.searchTerm)}
                  style={styleFeaturedButton(isReversed)}
                  styleHover={styleFeaturedButtonHover}
                  styleFocus={styleFeaturedButtonFocus}
                />
              </div>,
              <div key="content" style={styleDescriptionWrapper}>
                <p style={styleDescription}>
                  {collectionImage}
                  {f.description}
                </p>
              </div>,
            ]}
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
