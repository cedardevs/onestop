import React from 'react'
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

const styleTitle =  {
    background: '#026dab',
    fontFamily: fontFamilySerif(),
    padding: '.609em 1.018em',
    display: 'flex',
}

const styleImageContainer = {
  justifyContent: 'center',
  alignItems: 'center',
  flex: 2,
  cursor: 'pointer',
  height: '20em',
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

const styleFeaturedButton =  {
    flex: 1,
    textDecoration: 'underline',
    fontFamily: fontFamilySerif(),
    fontSize: '1.25em',
    width: '100%',
    background: 'transparent',
    color: 'inherit',
    textAlign: 'right',
    justifyContent: 'flex-end',
    padding: '.309em',
    margin: '.309em 0',
    marginRight: '1.018em',
    height: 'fit-content',
}

const styleFeaturedButtonFocus = {
  textDecoration: 'underline',
  background: 'transparent',
}

const styleFeaturedButtonHover = {
  background: 'transparent',
}

const FeaturedDatasets = props => {

  const search = query => {
    const {submit} = props
    submit(query)
  }

  // render:
  if (props.featured !== null && props.featured.length > 0) {
    const titleList = //(
      // <ul key="title-list" style={styleTitleList(collapseImage)}>
      props.featured.map((f, i, arr) => {
        const backgroundURL = processUrl(f.imageUrl)

        return (
          <li
            style={styleTitle}
            key={i}
          >
            <FlexRow
              style={{width: '100%'}}
              items={[
                <Button
                  key="button"
                  text={f.title}
                  title={`${f.title} Featured Data Search`}
                  onClick={() => search(f.searchTerm)}
                  style={styleFeaturedButton}
                  styleHover={styleFeaturedButtonHover}
                  styleFocus={styleFeaturedButtonFocus}
                />,
                <div key="image" style={styleImageContainer}>
                  <div style={styleImage} aria-hidden={true}>
                    <div
                      title={f.title}
                      style={styleFeaturedImage(backgroundURL)}
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
        <ul style={styleFeaturedDatasets}>
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
