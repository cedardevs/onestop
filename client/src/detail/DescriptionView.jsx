import React, {Component} from 'react'
import {processUrl} from '../utils/urlUtils'
import MapThumbnail from '../common/MapThumbnail'
import GranulesSummary from './GranulesSummary'
import Expandable from '../common/Expandable'
import DetailGrid from './DetailGrid'
import {fontFamilySerif} from '../utils/styleUtils'

const styleImage = {
  float: 'left',
  alignSelf: 'flex-start',
  margin: '0 1.618em 1.618em 0',
  width: '32%',
}

const styleMap = {
  margin: '0 0 0.618em 0',
  width: '100%',
  maxWidth: '500px',
}

const styleDescriptionWrapper = {
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

const styleExpandableWrapper = {
  margin: '0.618em',
}

const styleExpandableHeading = {
  backgroundColor: '#6792B5',
}

const styleExpandableH2 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  fontWeight: 'normal',
  letterSpacing: '0.05em',
  margin: 0,
  padding: '0.618em',
  color: 'white',
}

const styleExpandableContent = {
  color: '#000032',
  backgroundColor: '#eef5fb',
}

const styleContentPadding = {
  padding: '1.618em',
}

export default class DescriptionView extends Component {
  render() {
    const {item, totalGranuleCount, navigateToGranules} = this.props

    // thumbnail might be undefined or an empty string, so check for both
    const thumbnail =
      item.thumbnail && item.thumbnail.length > 0 ? item.thumbnail : undefined

    // geometry used, if available, to show map
    const geometry = item.spatialBounding

    // provide default description
    const description = item.description
      ? item.description
      : 'No description available'

    const filesContent = (
      <div style={styleContentPadding}>
        <GranulesSummary
          key="granule-summary-section"
          totalGranuleCount={totalGranuleCount}
          navigateToGranules={navigateToGranules}
        />
      </div>
    )

    const filesExpandable = (
      <Expandable
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        open={true}
        heading={<h2 style={styleExpandableH2}>Files</h2>}
        styleHeading={styleExpandableHeading}
        content={filesContent}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
      />
    )

    const citeAsStatements =
      item.citeAsStatements.length > 0 ? (
        item.citeAsStatements.map((statement, key) => {
          return (
            <div key={key} style={styleContentPadding}>
              {statement}
            </div>
          )
        })
      ) : (
        <div style={styleContentPadding}>No citations available.</div>
      )
    const citationExpandable = (
      <Expandable
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        heading={<h2 style={styleExpandableH2}>Citation</h2>}
        styleHeading={styleExpandableHeading}
        content={citeAsStatements}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
      />
    )

    const identifier = item.fileIdentifier ? (
      <div style={styleContentPadding}>{item.fileIdentifier}</div>
    ) : (
      <div style={styleContentPadding}>No file identifier available.</div>
    )
    const identifiersExpandable = (
      <Expandable
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        heading={<h2 style={styleExpandableH2}>Identifier</h2>}
        styleHeading={styleExpandableHeading}
        content={identifier}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
      />
    )

    const collectionImage = this.renderCollectionImage(thumbnail, geometry)
    const imageAndDescription = (
      <div style={styleDescriptionWrapper}>
        <p style={styleDescription}>
          {collectionImage}
          {description}
        </p>
      </div>
    )

    const expandableInformation = (
      <div>
        {filesExpandable}
        {citationExpandable}
        {identifiersExpandable}
      </div>
    )

    return (
      <DetailGrid
        grid={[ [ imageAndDescription, expandableInformation ] ]}
        colWidths={[ {sm: 8}, {sm: 4} ]}
      />
    )
  }

  renderCollectionImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl) {
      return (
        <img
          style={styleImage}
          src={imgUrl}
          alt="collection result image"
          aria-hidden="true"
        />
      )
    }
    else if (geometry) {
      return (
        <div style={styleMap}>
          <MapThumbnail geometry={geometry} interactive={false} />
        </div>
      )
    }
    else {
      return <div style={styleMap}>No preview image or map available.</div>
    }
  }
}
