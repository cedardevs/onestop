import React from 'react'
import dlStep1 from '../../../img/dl-step1.png'
import {fontFamilyMonospace} from '../../utils/styleUtils'
import Expandable from '../common/ui/Expandable'
import {SiteColors} from '../../style/defaultStyles'

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
  minWidth: '20em',
  fontFamily: fontFamilyMonospace(),
  fontSize: '0.618em',
  color: '#222',
  background: '#F9F9F9',
  borderRadius: '0.309em',
  border: '1px solid #EFEFEF',
  padding: '1em',
}

const styleInlineCode = {
  fontFamily: fontFamilyMonospace(),
  color: '#222',
  background: '#F9F9F9',
}

const styleConsoleExample = {
  minWidth: '20em',
  fontFamily: fontFamilyMonospace(),
  fontSize: '0.618em',
  color: '#F9F9F9',
  background: '#222',
  borderRadius: '0.309em',
  border: '1px solid #EFEFEF',
  padding: '1em',
}

const styleDownloadSteps = {
  display: 'flex',
  flexFlow: 'row wrap',
  justifyContent: 'space-between',
  flexWrap: 'wrap',
}

const styleWarning = {
  fontWeight: 'bold',
  color: SiteColors.WARNING,
}

const styleStep = {
  margin: '0 1em 1em 0',
  width: '30%',
}

const styleStepLabel = {
  marginBottom: '0.309em',
  fontWeight: 'bold',
}

export default class ScriptDownloaderInfo extends React.Component {
  render() {
    const step1 = (
      <li key="links-downloader-info-step1" style={styleStep}>
        <div>
          <div style={styleStepLabel}>Choose link type</div>
          <div>Link type is described by source and protocol.</div>
          <img
            src={dlStep1}
            style={styleStep1Image}
            alt="FTP link type selected with Links button enabled"
          />
        </div>
      </li>
    )

    const step2 = (
      <li key="links-downloader-info-step2" style={styleStep}>
        <div>
          <div style={styleStepLabel}>Download text file</div>
          <div>
            Use the Links buttons to download the file (e.g. - onestop_ftp.txt)
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
      </li>
    )

    const step3 = (
      <li key="links-downloader-info-step3" style={styleStep}>
        <div>
          <div style={styleStepLabel}>Use download utility</div>
          <div>
            For example, <span style={styleInlineCode}>wget</span> is a command
            line utility for non-interactive download of files.
          </div>
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
      </li>
    )

    const downloadSteps = (
      <ol style={styleDownloadSteps}>
        {step1}
        {step2}
        {step3}
      </ol>
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
