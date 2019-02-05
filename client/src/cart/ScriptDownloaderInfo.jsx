import React from 'react'
import FlexRow from '../common/FlexRow'
import dlStep1 from '../../img/dl-step1.png'
import {arrow_right, SvgIcon} from '../common/SvgIcon'
import {fontFamilyMonospace} from '../utils/styleUtils'
import Expandable from '../common/Expandable'

const styleArrowIconWrapper = {
  padding: '1em',
}

const styleArrowIcon = {
  fill: '#636363',
}

const styleExpandableInfoContent = {
  background: '#efefef',
  borderRadius: '0.309em',
  padding: '0.618em',
  margin: '0.618em 0',
}

const styleStep1Image = {
  height: '3em',
  display: 'block',
}

const styleFileExample = {
  fontFamily: fontFamilyMonospace(),
  fontSize: '0.618em',
  color: '#222',
  background: '#F9F9F9',
  borderRadius: '0.309em',
  border: '1px solid #EFEFEF',
  padding: '1em',
}

const styleConsoleExample = {
  fontFamily: fontFamilyMonospace(),
  fontSize: '0.618em',
  color: '#F9F9F9',
  background: '#222',
  borderRadius: '0.309em',
  border: '1px solid #EFEFEF',
  padding: '1em',
}

const styleDownloadSteps = {
  justifyContent: 'space-between',
  alignItems: 'center',
}

const styleWarning = {
  fontWeight: 'bold',
  color: 'red',
}

const styleStep = {
  display: 'flex',
  flexDirection: 'column',
  alignSelf: 'flex-start',
}

const styleStepLabel = {
  marginBottom: '0.309em',
  fontWeight: 'bold',
}

export default class ScriptDownloaderInfo extends React.Component {
  render() {
    const step1 = (
      <div key="links-downloader-info-step1" style={styleStep}>
        <div style={styleStepLabel}>1. Choose source and protocol</div>
        <img src={dlStep1} style={styleStep1Image} />
      </div>
    )

    const nextStep1 = (
      <SvgIcon
        key="links-downloader-info-nextstep1"
        size="1.618em"
        wrapperStyle={styleArrowIconWrapper}
        style={styleArrowIcon}
        path={arrow_right}
        verticalAlign="initial"
      />
    )

    const step2 = (
      <div key="links-downloader-info-step2" style={styleStep}>
        <div style={styleStepLabel}>
          2. Obtain "Links" (e.g. - onestop_ftp.txt)
        </div>
        <div style={styleFileExample}>
          ftp://ftp.nodc.noaa.gov/.../1.nc<br />
          ftp://ftp.nodc.noaa.gov/.../2.nc<br />
          ftp://ftp.nodc.noaa.gov/.../3.nc<br />
          <br />
          <br />
          <br />
        </div>
      </div>
    )

    const nextStep2 = (
      <SvgIcon
        key="links-downloader-info-nextstep2"
        size="1.618em"
        wrapperStyle={styleArrowIconWrapper}
        style={styleArrowIcon}
        path={arrow_right}
        verticalAlign="initial"
      />
    )

    const step3 = (
      <div key="links-downloader-info-step3" style={styleStep}>
        <div style={styleStepLabel}>3. Use download utility</div>
        <div style={styleConsoleExample}>
          &gt; wget -i onestop_ftp.txt
          <br />
          <br />
          ...
          <br />
          <br />
          Downloaded: 3 files, 1.5M in 2.5s (600 KB/s)
          <br />
          &gt;
        </div>
      </div>
    )

    const downloadSteps = (
      <FlexRow
        style={styleDownloadSteps}
        items={[ step1, nextStep1, step2, nextStep2, step3 ]}
      />
    )

    const expandableInfo = (
      <Expandable
        key="dsmm-info"
        open={this.props.show}
        content={
          <div style={styleExpandableInfoContent}>
            <p>
              If OneStop contains download links for a file, they can be added
              to this cart. You can create a list of links by selecting a source
              in the dropdown. For example, the following 3 steps show how to
              download FTP links and retrieve files using <code>wget</code>.
            </p>
            <p>
              <span style={styleWarning}>Warning: </span>These links are not
              guaranteed to be direct downloads.
            </p>
            <div>{downloadSteps}</div>
          </div>
        }
      />
    )
    return expandableInfo
  }
}
