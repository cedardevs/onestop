import React from 'react'
import CartSelect from './CartSelect'
import FileSaver from 'file-saver'
import Button from '../common/input/Button'
import download from 'fa/download.svg'
import {COLOR_GREEN, COLOR_GREEN_LIGHT} from '../common/defaultStyles'
import _ from 'lodash'
import moment from 'moment/moment'
import FlexRow from '../common/FlexRow'

const styleWgetScriptButton = {
  fontSize: '1em',
  display: 'inline-flex',
  padding: '0.309em 0.618em 0.309em 0.309em',
  marginLeft: '0.618em',
  background: `${COLOR_GREEN_LIGHT}`,
}

const styleWgetScriptButtonText = {
  whiteSpace: 'nowrap',
}

const styleWgetScriptButtonHover = {
  background: `linear-gradient(${COLOR_GREEN_LIGHT}, ${COLOR_GREEN})`,
}

const styleWgetScriptIcon = {
  width: '1.618em',
  height: '1.618em',
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
            acc.push({value: currIndex, label: label, count: count})
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
    const selectedGranuleIds = Object.keys(selectedGranules)
    let downloadLinks = []
    selectedGranuleIds.forEach(granuleId => {
      const selectedGranuleLinks = selectedGranules[granuleId].links
      const matchingLink = selectedGranuleLinks.find(link => {
        return (
          link.linkProtocol === protocol &&
          link.linkName === source && // linkName ~ "source"
          link.linkFunction === 'download' &&
          link.linkUrl
        )
      })
      if (matchingLink) {
        downloadLinks.push(matchingLink.linkUrl)
      }
    })
    return _.uniq(downloadLinks)
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
    const fileName = `onestop_wget_${fileLabelClean}_${fileDate}`
    FileSaver.saveAs(blob, fileName)
  }

  // set appropriate initial conditions for internal state
  constructor(props) {
    super(props)
    this.state = {
      sourcesAndProtocols: [],
      selectedSourceAndProtocol: 0,
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
    })
  }

  render() {
    // sources and protocol array is managed in React component state
    const {sourcesAndProtocols} = this.state

    // organize sources + protocols into groups
    const protocolOptGroups = this.getProtocolOptGroups(sourcesAndProtocols)

    // granule download select menu for cart
    const cartSelect = (
      <CartSelect
        key="wgetScriptSelect"
        style={{width: '100%'}}
        options={protocolOptGroups}
        onChange={this.handleProtocolSourceChange}
      />
    )

    // granule download button
    const downloadButton = (
      <Button
        key="wgetScriptButton"
        style={styleWgetScriptButton}
        styleHover={styleWgetScriptButtonHover}
        title={'Download wget script'}
        text={'Download wget script'}
        styleText={styleWgetScriptButtonText}
        icon={download}
        styleIcon={styleWgetScriptIcon}
        onClick={this.downloadScript}
      />
    )

    return (
      <div>
        <FlexRow items={[ cartSelect, downloadButton ]} />
      </div>
    )
  }
}
