import React from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import DescriptionView from './DescriptionView'
import OverviewView from './OverviewView'
import AccessView from './AccessView'
import VideoView from './VideoView'
import Tabs from './Tabs'
import {boxShadow} from '../../../style/defaultStyles'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {identifyProtocol} from '../../../utils/resultUtils'
import {asterisk, SvgIcon} from '../../common/SvgIcon'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleDetailWrapper = {
  color: 'black',
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  backgroundColor: 'white',
}

const styleLoadingMessage = {
  textAlign: 'center',
}

const styleErrorMessage = {
  textAlign: 'center',
}

const styleTitle = {
  margin: 0,
  padding: '0.691em 0.691em 1.691em 0.691em',
  backgroundColor: '#8cb9d8',
  color: '#000032',
  background: 'linear-gradient(#8cb9d8 0%, white 100%)',
  fontFamily: fontFamilySerif(),
  fontSize: '1.4em',
  fontWeight: 'bold',
  outline: 'none',
}

const styleContent = {
  fill: '#000032',
  color: '#000032',
  backgroundColor: 'white',
}

const styleHeadingSpan = {
  padding: '0.309em',
}

//-- Component
class Detail extends React.Component {
  render() {
    const {id, item, loading} = this.props

    let headingMessage = null
    let content = null
    if (loading) {
      headingMessage = (
        <div style={styleLoadingMessage}>
          <SvgIcon
            style={{animation: 'rotation 2s infinite linear'}}
            path={asterisk}
            size=".9em"
          />&nbsp;{' '}
          <span role="alert" aria-live="polite">
            Loading...
          </span>
        </div>
      )
    }
    else if (item) {
      headingMessage = item.title
      let tabData = [
        {
          title: 'Overview',
          content: <OverviewView item={item} />,
        },
        {
          title: 'Access',
          content: <AccessView item={item} />,
        },
        // {
        //   title: 'Keywords',
        //   content: <KeywordsView item={item} />,
        // },
      ]

      const videoLinks = item.links.filter(
        link => identifyProtocol(link).label === 'Video'
      )
      const showVideoTab = videoLinks.length > 0
      if (showVideoTab) {
        tabData.push({
          title: videoLinks.length === 1 ? 'Video' : 'Videos',
          content: <VideoView links={videoLinks} />,
        })
      }
      content = (
        <div>
          <DescriptionView item={item} itemUuid={id} />
          <Tabs
            style={{display: 'flex'}}
            styleContent={styleContent}
            data={tabData}
            activeIndex={0}
          />
        </div>
      )
    }
    else {
      //   // TODO error style? actually report an error in the flow if the collection is not found when search returns? - I can verify this now!!

      headingMessage = (
        <span style={styleErrorMessage}>
          There was a problem loading your collection.
        </span>
      )
    }

    return (
      <div style={styleCenterContent}>
        <div style={styleDetailWrapper}>
          <h1 key="filtersH1" style={styleTitle}>
            <div style={styleHeadingSpan}>
              <span role="alert" aria-live="polite">
                {headingMessage}
              </span>
            </div>
          </h1>
          {content}
        </div>
      </div>
    )
  }
}

Detail.propTypes = {
  item: PropTypes.object,
}

export default Detail
