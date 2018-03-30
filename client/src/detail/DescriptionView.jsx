import React, {Component} from 'react'
import {processUrl} from '../utils/urlUtils'
import MapThumbnail from '../common/MapThumbnail'
import Expandable from '../common/Expandable'
import DetailGrid from './DetailGrid'

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
  alignItems: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
}

const styleDescription = {
  width: '100%',
  padding: '1.618em',
  margin: 0,
}

export default class DescriptionView extends Component {
  render() {
    const {item} = this.props

    // thumbnail might be undefined or an empty string, so check for both
    const thumbnail =
      item.thumbnail && item.thumbnail.length > 0 ? item.thumbnail : undefined

    // geometry used, if available, to show map
    const geometry = item.spatialBounding

    // provide default description
    const description = item.description
      ? item.description
      : 'No description available'

    const styleExpandableHeading = {
      // padding: '0.618em',
      backgroundColor: '#6792B5',
    }

    const styleExpandableContent = {
      // padding: '0.618em',
    }

    console.log('item:', item)

    const citeAsStatements =
      item.citeAsStatements.length > 0
        ? item.citeAsStatements.map((statement, key) => {
            return <div key={key}>{statement}</div>
          })
        : 'No citations available.'
    const citationExpandable = (
      <Expandable
        showArrow={true}
        heading={
          <div style={{padding: '0.618em', color: 'white', fontWeight: 'bold'}}>
            Citation
          </div>
        }
        styleHeading={styleExpandableHeading}
        content={
          <div
            style={{
              padding: '1.618em',
              color: '#000032',
              backgroundColor: '#eef5fb',
            }}
          >
            {citeAsStatements}
          </div>
        }
        styleContent={styleExpandableContent}
      />
    )

    const identifier = item.fileIdentifier
      ? item.fileIdentifier
      : 'No file identifier available.'
    const identifiersExpandable = (
      <Expandable
        showArrow={true}
        heading={
          <div style={{padding: '0.618em', color: 'white', fontWeight: 'bold'}}>
            Identifier
          </div>
        }
        styleHeading={styleExpandableHeading}
        content={
          <div
            style={{
              padding: '1.618em',
              color: '#000032',
              backgroundColor: '#eef5fb',
            }}
          >
            {identifier}
          </div>
        }
        styleContent={styleExpandableContent}
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
    // const gridImageAndDescription = [
    //     [collectionImage, description]
    // ]
    // const imageAndDescription = (
    //     <DetailGrid grid={gridImageAndDescription} colWidths={[{sm:4},{sm:8}]}/>
    // )

    const expandableInformation = (
      <div>
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

    // return (
    //   <div style={{display: 'flex'}}>
    //     <p style={styleDescription}>
    //       {collectionImage}
    //       {description}
    //     </p>
    //     {expandableInformation}
    //   </div>
    // )
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
