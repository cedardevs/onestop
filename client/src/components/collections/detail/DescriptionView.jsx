import React from 'react'
import {processUrl, getApiRegistryPath} from '../../../utils/urlUtils'
import MapThumbnail from '../../common/MapThumbnail'
import GranulesSummaryContainer from './GranulesSummaryContainer'
import Expandable from '../../common/ui/Expandable'
import DetailGrid from './DetailGrid'
import {fontFamilySerif} from '../../../utils/styleUtils'
import A from '../../common/link/Link'
import {SiteColors} from '../../../style/defaultStyles'
import Meta from '../../helmet/Meta'

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

const styleExpandableFocused = {
  background: 'linear-gradient(to right, rgba(0,0,0,0.4) 0%, transparent 100%)',
  outline: 'none',
}

const styleExpandableHeading = {
  backgroundColor: '#3c6280',
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

const styleIdentifierHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  margin: '0 0 0.618em 0',
}

const styleIdentifier = {
  margin: '0 0 0.618em 0',
}

const styleIdentifierLast = {
  margin: 0,
}

const styleMetadataSummary = {
  textAlign: 'center',
}

export default class DescriptionView extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      filesExpandable: true,
      citationExpandable: false,
      identifiersExpandable: false,
      metadataExpandable: false,
    }
  }

  handleExpandableToggle = event => {
    // prevent focus-change state from disrupting if each expandable is open
    let toggledElement = event.value
    this.setState({
      [toggledElement]: event.open,
    })
  }

  render() {
    const {item, itemUuid} = this.props

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
        <GranulesSummaryContainer key="granule-summary-section" />
      </div>
    )

    const filesExpandable = (
      <Expandable
        styleFocus={styleExpandableFocused}
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        heading={<h2 style={styleExpandableH2}>Files</h2>}
        styleHeading={styleExpandableHeading}
        content={filesContent}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
        value="filesExpandable"
        open={this.state.filesExpandable}
        onToggle={this.handleExpandableToggle}
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
        styleFocus={styleExpandableFocused}
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        heading={<h2 style={styleExpandableH2}>Citation</h2>}
        styleHeading={styleExpandableHeading}
        content={citeAsStatements}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
        value="citationExpandable"
        open={this.state.citationExpandable}
        onToggle={this.handleExpandableToggle}
      />
    )

    const doi = item.doi ? (
      <A
        target="_blank"
        href={`https://doi.org/${item.doi}`}
        style={{color: SiteColors.LINK}}
      >
        {item.doi}
      </A>
    ) : (
      'Not available.'
    )
    const fileIdentifier = item.fileIdentifier
      ? item.fileIdentifier
      : 'Not available.'

    const identifiers = (
      <div style={styleContentPadding}>
        <h3 style={styleIdentifierHeading}>File Identifier</h3>
        <p style={styleIdentifier}>{fileIdentifier}</p>
        <h3 style={styleIdentifierHeading}>DOI</h3>
        <p style={styleIdentifierLast}>{doi}</p>
      </div>
    )

    const identifiersExpandable = (
      <Expandable
        styleFocus={styleExpandableFocused}
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        heading={<h2 style={styleExpandableH2}>Identifier(s)</h2>}
        styleHeading={styleExpandableHeading}
        content={identifiers}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
        value="identifiersExpandable"
        open={this.state.identifiersExpandable}
        onToggle={this.handleExpandableToggle}
      />
    )

    const xmlLink = (
      <a
        href={
          getApiRegistryPath() + '/metadata/collection/' + itemUuid + '/raw'
        }
        target={'_blank'}
      >
        Download the full metadata here
      </a>
    )

    const metadataContent = (
      <div style={styleContentPadding}>
        <div style={styleMetadataSummary}>{xmlLink}</div>
      </div>
    )

    const metadataExpandable = (
      <Expandable
        styleFocus={styleExpandableFocused}
        styleWrapper={styleExpandableWrapper}
        showArrow={true}
        heading={<h2 style={styleExpandableH2}>Metadata Access</h2>}
        styleHeading={styleExpandableHeading}
        content={metadataContent}
        styleContent={styleExpandableContent}
        borderRadius={'1em'}
        value="metadataExpandable"
        open={this.state.metadataExpandable}
        onToggle={this.handleExpandableToggle}
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
        {metadataExpandable}
        {citationExpandable}
        {identifiersExpandable}
      </div>
    )

    return (
      <div>
        <Meta
          title={item.title}
          formatTitle={true}
          description={item.description}
          thumbnail={processUrl(thumbnail)}
          item={item}
          itemUuid={itemUuid}
        />
        <DetailGrid
          grid={[ [ imageAndDescription, expandableInformation ] ]}
          colWidths={[ {sm: 8}, {sm: 4} ]}
        />
      </div>
    )
  }

  renderCollectionImage(thumbnail, geometry) {
    const imgUrl = processUrl(thumbnail)
    if (imgUrl) {
      return <img style={styleImage} src={imgUrl} alt="" aria-hidden="true" />
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
