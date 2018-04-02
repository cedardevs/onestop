import React, {Component} from 'react'
import PropTypes from 'prop-types'
import DescriptionView from './DescriptionView'
import OverviewView from './OverviewView'
import AccessView from './AccessView'
import VideoView from './VideoView'
import Tabs from './Tabs'
import {boxShadow} from '../common/defaultStyles'
import Keywords from './Keywords'
import KeywordsView from './KeywordsView'

//-- Styles

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
}

const styleDetailWrapper = {
  color: 'black',
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  // Note: margins on this element are temporarily needed to show the box shadow correctly. I expect this to change when we restructure the details page to get rid of tabs, as well as possibly when we update the router.
  marginRight: '3px',
  marginLeft: '1px',
  backgroundColor: 'white',
}

const styleTitle = {
  fontSize: '1.5em',
  margin: 0,
  padding: '1em',
  backgroundColor: '#8cb9d8',
  color: '#000032',
  borderRadius: '0 0 0 1.618em'
}

const styleContent = {
  fill: '#000032',
  color: '#000032',
  backgroundColor: 'white',
}

//-- Component
class Detail extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    const {id, item, loading, totalGranuleCount, showGranules} = this.props

    if (loading) {
      return (
        <div style={styleDetailWrapper}>
          <h1>Loading...</h1>
        </div>
      )
    }

    if (!item) {
      // TODO error style? actually report an error in the flow if the collection is not found when search returns?
      return (
        <div style={styleDetailWrapper}>
          <h1>There was a problem loading your collection.</h1>
        </div>
      )
    }

    let tabData = [
      {
        title: 'Overview',
        content: (
          <OverviewView
            item={item}
            totalGranuleCount={totalGranuleCount}
          />
        ),
      },
      {
        title: 'Access',
        content: <AccessView item={item} />,
      },
      {
        title: 'Keywords',
        content: <KeywordsView item={item} />,
      },
    ]

    const videoLinks = item.links.filter(
      link => link.linkProtocol === 'video:youtube'
    )
    const showVideoTab = videoLinks.length > 0
    if (showVideoTab) {
      tabData.push({
        title: videoLinks.length === 1 ? 'Video' : 'Videos',
        content: <VideoView links={videoLinks} />,
      })
    }

    return (
      <div style={styleCenterContent}>
        <div style={styleDetailWrapper}>
          <h1 style={styleTitle}>{item.title}</h1>
          <DescriptionView item={item} totalGranuleCount={totalGranuleCount} granuleSearch={() => {
            showGranules(id)
          }}/>
          <Tabs
            style={{display: 'flex'}}
            styleContent={styleContent}
            data={tabData}
            activeIndex={0}
          />
        </div>
      </div>
    )
  }
}

Detail.propTypes = {
  item: PropTypes.object,
}

export default Detail
