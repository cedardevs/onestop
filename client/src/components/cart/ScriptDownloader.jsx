import React from 'react'
import CartSelect from './CartSelect'
import FileSaver from 'file-saver'
import Button from '../common/input/Button'
import download from 'fa/download.svg'
import _ from 'lodash'
import moment from 'moment/moment'
import FlexRow from '../common/ui/FlexRow'
import {granuleDownloadableLinks} from '../../utils/cartUtils'
import {info_circle, SvgIcon} from '../common/SvgIcon'
import ScriptDownloaderInfo from './ScriptDownloaderInfo'
import * as util from '../../utils/resultUtils'

const styleLinksButton = {
  fontSize: '1em',
  display: 'inline-flex',
  padding: '0.309em 0.618em 0.309em 0.309em',
  marginLeft: '0.618em',
}

const styleLinksButtonText = {
  whiteSpace: 'nowrap',
}

const styleLinksButtonFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.118em',
}

const styleLinksButtonDisabled = {
  fontStyle: 'italic',
}

const styleLinksIcon = {
  width: '1.618em',
  height: '1.618em',
  marginRight: '0.309em',
}

const styleInfoButton = {
  marginLeft: '0.309em',
  padding: '0.309em 0.618em 0.309em 0.309em',
  border: 0,
  boxSizing: 'content-box',
  background: 'none',
  color: 'inherit',
  font: 'inherit',
  lineHeight: 'normal',
  overflow: 'visible',
  userSelect: 'none',
  whiteSpace: 'nowrap',
}

const styleInfoButtonFocused = {
  outline: '2px dashed #00002c',
}

const styleShowHideText = {
  marginLeft: '0.309em',
  textDecoration: 'underline',
}

const styleDownloadLabel = {
  whiteSpace: 'nowrap',
  alignSelf: 'center',
  marginRight: '0.309em',
}

export default class ScriptDownloader extends React.Component {
  // Selected granules, whether retrieved from local storage or
  // elsewhere, are stored in the form of a Map, where the keys
  // are the granule ID, and the values are the full granule objects.
  // If unavailable, selected granules should be an empty map `{}`.

  // This method takes that map and re-organizes the data structure
  // into an array of unique combinations of `[{source: _, protocol: _}, ...]`
  getSourcesAndProtocols = selectedGranules => {
    const downloadableSrcAndProto = Object.keys(
      selectedGranules
    ).map(granuleId => {
      return (
        selectedGranules[granuleId].links
          // filter out any combination without a 'download' function
          .filter(link => {
            return link.linkFunction === 'download'
          })
          // extract only the information we care about being unique
          .map(link => {
            return {
              source: link.linkName,
              protocol: link.linkProtocol,
            }
          })
      )
    })
    const uniqueSourcesAndProtocols = downloadableSrcAndProto.reduce(
      (acc, curr) => {
        return _.unionWith(acc, curr, _.isEqual)
      }
    )
    return uniqueSourcesAndProtocols
  }

  // extract unique protocols from distinct list of sources + protocols
  getUniqueProtocols = sourcesAndProtocols => {
    return _.uniq(
      sourcesAndProtocols.map(e => {
        return e.protocol
      })
    )
  }

  // organize sources + protocols into a structure our select menu understands
  getProtocolOptGroups = sourcesAndProtocols => {
    // extract unique protocols
    const uniqueProtocols = this.getUniqueProtocols(sourcesAndProtocols)

    return uniqueProtocols.map(protocol => {
      const protocolOptions = sourcesAndProtocols.reduce(
        (acc, curr, currIndex) => {
          if (curr.protocol === protocol) {
            const count = this.countLinks(curr.source, curr.protocol)
            const label = curr.source
            if (count > 0) {
              const functionalLabel =
                label || util.identifyProtocol({linkProtocol: protocol}).label
              acc.push({value: currIndex, label: functionalLabel, count: count})
            }
          }
          return acc
        },
        []
      )
      return {
        label: protocol,
        options: protocolOptions,
      }
    })
  }

  // This method takes a given source + protocol, iterates through selected granules,
  // and extracts links from granules with matching source + protocol
  // if they have a url and download type.
  getLinks = (source, protocol) => {
    const {selectedGranules} = this.props
    const granules = Object.values(selectedGranules)
    return granuleDownloadableLinks(granules, protocol, source)
  }

  // this method simply counts the number of valid links
  // associated with a source + protocol, by leveraging the `getLinks` method
  countLinks = (source, protocol) => {
    return this.getLinks(source, protocol).length
  }

  // This method constructs a newline delimited file of links, given
  // the available sources + protocols and the selected source and protocol
  // from the select menu. A Blob is used along with the `file-saver` dependency
  // which allows the file to be saved in an action from the browser.
  downloadScript = () => {
    const {sourcesAndProtocols, selectedSourceAndProtocol} = this.state
    const sourceAndProtocol = sourcesAndProtocols[selectedSourceAndProtocol]
    const source = sourceAndProtocol.source
    const protocol = sourceAndProtocol.protocol
    const links = this.getLinks(source, protocol)
    let blob = new Blob([ links.join('\n') ], {
      type: 'text/plain;charset=utf-8',
    })

    let fileLabel = `${source}_${protocol}`
    if (source === protocol) {
      fileLabel = source
    }
    const fileLabelClean = fileLabel
      .replace(/[^a-z0-9_\-]/gi, '_')
      .toLowerCase()
    const fileDate = moment.utc().format('YYYY-MM-DDTHH-mm-ss[Z]')
    const fileName = `onestop_${fileLabelClean}_${fileDate}`
    FileSaver.saveAs(blob, fileName)
  }

  // set appropriate initial conditions for internal state
  constructor(props) {
    super(props)
    this.state = {
      sourcesAndProtocols: [],
      selectedSourceAndProtocol: 0,
      menuOpen: false,
      valueSelected: false,
    }
  }

  // initially set component state based on selected granules
  componentDidMount() {
    const {selectedGranules} = this.props
    const sourcesAndProtocols = this.getSourcesAndProtocols(selectedGranules)
    this.setState({
      sourcesAndProtocols: sourcesAndProtocols,
    })
  }

  // update component state based on selected granules when selected granules change
  componentDidUpdate(prevProps) {
    const {selectedGranules} = this.props
    if (selectedGranules !== prevProps.selectedGranules) {
      const sourcesAndProtocols = this.getSourcesAndProtocols(selectedGranules)
      this.setState({
        sourcesAndProtocols: sourcesAndProtocols,
      })
    }
  }

  // the user has selected another source + protocol combo from the select menu
  handleProtocolSourceChange = option => {
    const sourcesAndProtocolsIndex = option.value
    this.setState({
      selectedSourceAndProtocol: sourcesAndProtocolsIndex,
      valueSelected: true,
    })
  }

  handleOnMenuOpen = () => {
    this.setState(prevState => {
      return {
        ...prevState,
        menuOpen: true,
      }
    })
  }

  handleOnMenuClose = () => {
    this.setState(prevState => {
      return {
        ...prevState,
        menuOpen: false,
      }
    })
  }

  UNSAFE_componentWillMount() {
    this.setState({
      showInfo: false,
      focusingShowInfo: false,
    })
  }

  handleShowInfo = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        showInfo: true,
      }
    })
  }

  handleHideInfo = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        showInfo: false,
      }
    })
  }

  handleShowInfoFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingShowInfo: true,
      }
    })
  }

  handleShowInfoBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingShowInfo: false,
      }
    })
  }

  render() {
    // sources and protocol array is managed in React component state
    const {sourcesAndProtocols, valueSelected, menuOpen} = this.state

    // organize sources + protocols into groups
    const protocolOptGroups = this.getProtocolOptGroups(sourcesAndProtocols)

    // if there is nothing downloadable in the selected granules,
    // this component should not be rendered
    if (protocolOptGroups.length < 1) {
      return null
    }

    const downloadLabel = (
      <label
        id="cartDownloadOptionsLabel"
        key="cartDownloadOptionsLabel"
        style={styleDownloadLabel}
      >
        Download Options:
      </label>
    )

    // select menu for downloadable links file for cart
    const cartSelect = (
      <CartSelect
        key="linksSourceSelect"
        style={{width: '100%', marginLeft: '0.309em'}}
        options={protocolOptGroups}
        onChange={this.handleProtocolSourceChange}
        onMenuOpen={this.handleOnMenuOpen}
        onMenuClose={this.handleOnMenuClose}
        isMenuOpen={menuOpen}
      />
    )

    // file of granule links: download button
    const downloadButton = (
      <Button
        key="linksDownloadButton"
        style={styleLinksButton}
        styleFocus={styleLinksButtonFocus}
        title={'Download Links as Text File'}
        text={'Links'}
        styleText={styleLinksButtonText}
        icon={download}
        styleIcon={styleLinksIcon}
        onClick={this.downloadScript}
        disabled={!!!valueSelected}
        styleDisabled={styleLinksButtonDisabled}
      />
    )

    const styleInfoButtonMerged = {
      ...styleInfoButton,
      ...(this.state.focusingShowInfo ? styleInfoButtonFocused : {}),
    }

    const infoButton = (
      <button
        key="links-downloader-info-button"
        aria-label="Links download information"
        style={styleInfoButtonMerged}
        aria-expanded={this.state.showInfo}
        onClick={
          this.state.showInfo ? this.handleHideInfo : this.handleShowInfo
        }
        onFocus={this.handleShowInfoFocus}
        onBlur={this.handleShowInfoBlur}
      >
        <SvgIcon path={info_circle} size="1em" />
        <span style={styleShowHideText}>
          {this.state.showInfo ? 'hide ' : 'show '}info
        </span>
      </button>
    )

    return (
      <div>
        <FlexRow
          items={[ downloadLabel, cartSelect, downloadButton, infoButton ]}
        />
        <ScriptDownloaderInfo show={this.state.showInfo} />
      </div>
    )
  }
}
