import React from 'react'
import FileSaver from 'file-saver'
import Button from '../common/input/Button'
import download from 'fa/download.svg'
import {COLOR_GREEN, COLOR_GREEN_LIGHT} from '../common/defaultStyles'
import _ from 'lodash'
import moment from 'moment/moment'

const styleWgetScriptButton = {
  fontSize: '1em',
  display: 'inline-flex',
  padding: '0.309em 0.618em 0.309em 0.309em',
  background: `${COLOR_GREEN_LIGHT}`,
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
  getUniqueDownloadableSourcesAndProtocols = selectedGranules => {
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
    // merge into unique combinations of source + protocol
    const uniqueSourcesAndProtocols = downloadableSrcAndProto.reduce(
      (acc, curr) => {
        return _.unionWith(acc, curr, _.isEqual)
      }
    )
    return uniqueSourcesAndProtocols
  }

  getDownloadLinksForSourceAndProtocol = (source, protocol) => {
    const {selectedGranules} = this.props
    const selectedGranuleIds = Object.keys(selectedGranules)
    let downloadLinks = []
    selectedGranuleIds.forEach(granuleId => {
      const selectedGranuleLinks = selectedGranules[granuleId].links
      const matchingLink = selectedGranuleLinks.find(link => {
        return (
          link.linkProtocol === protocol &&
          link.linkName === source &&
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

  downloadWgetScript = () => {
    const {sourcesAndProtocols, selectedSourceAndProtocol} = this.state
    const sourceAndProtocol = sourcesAndProtocols[selectedSourceAndProtocol]
    const source = sourceAndProtocol.source
    const protocol = sourceAndProtocol.protocol
    const links = this.getDownloadLinksForSourceAndProtocol(source, protocol)
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

  constructor(props) {
    super(props)
    this.state = {
      sourcesAndProtocols: [],
      selectedSourceAndProtocol: 0,
    }
  }

  componentDidMount() {
    const {selectedGranules} = this.props
    const sourcesAndProtocols = this.getUniqueDownloadableSourcesAndProtocols(
      selectedGranules
    )
    this.setState({
      sourcesAndProtocols: sourcesAndProtocols,
    })
  }

  componentDidUpdate(prevProps) {
    const {selectedGranules} = this.props
    if (selectedGranules !== prevProps.selectedGranules) {
      const sourcesAndProtocols = this.getUniqueDownloadableSourcesAndProtocols(
        selectedGranules
      )
      this.setState({
        sourcesAndProtocols: sourcesAndProtocols,
      })
    }
  }

  handleProtocolSourceChange = event => {
    const sourcesAndProtocolsIndex = event.target.value
    this.setState({
      selectedSourceAndProtocol: sourcesAndProtocolsIndex,
    })
  }

  render() {
    const {sourcesAndProtocols, selectedSourceAndProtocol} = this.state

    const uniqueProtocols = _.uniq(
      sourcesAndProtocols.map(e => {
        return e.protocol
      })
    )

    let protocolOptGroups = uniqueProtocols.map(protocol => {
      const protocolOptions = sourcesAndProtocols.reduce(
        (acc, curr, currIndex) => {
          if (curr.protocol === protocol) {
            acc.push(
              <option key={currIndex} value={currIndex}>
                {curr.source}
              </option>
            )
          }
          return acc
        },
        []
      )
      return (
        <optgroup key={protocol} label={`Protocol: ${protocol}`}>
          {protocolOptions}
        </optgroup>
      )
    })

    return (
      <div>
        <Button
          key="wgetScriptButton"
          style={styleWgetScriptButton}
          styleHover={styleWgetScriptButtonHover}
          title={'Download wget script'}
          text={'Download wget script'}
          icon={download}
          styleIcon={styleWgetScriptIcon}
          onClick={this.downloadWgetScript}
        />
        <select
          key="protocolSourceSelect"
          value={selectedSourceAndProtocol}
          onChange={this.handleProtocolSourceChange}
        >
          {protocolOptGroups}
        </select>
      </div>
    )
  }
}
