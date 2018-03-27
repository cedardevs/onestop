import React, {Component} from 'react'
import PropTypes from 'prop-types'
import Tabs from './Tabs'
import SummaryView from './SummaryView'
import DescriptionView from './DescriptionView'
// import GranuleViewContainer from './GranuleTab/GranuleViewContainer'
import AccessView from './AccessView'
import VideoView from './VideoView'
import {boxShadow} from '../common/defaultStyles'

//-- Styles

const styleDetailWrapper = {
  color: 'black',
  maxWidth: '80em',
  width: '80em',
  boxShadow: boxShadow,
  // Note: margins on this element are temporarily needed to show the box shadow correctly. I expect this to change when we restructure the details page to get rid of tabs, as well as possibly when we update the router.
  marginRight: '3px',
  marginLeft: '1px',
}

const styleTitle = {
  fontSize: '1.5em',
  margin: 0,
  padding: '1em',
  backgroundColor: '#8cb9d8',
  color: '#000032',
}

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
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
        title: 'Summary',
        content: (
          <SummaryView
            item={item}
            totalGranuleCount={totalGranuleCount}
            granuleSearch={() => {
              showGranules(id)
            }}
          />
        ),
      },
      {
        title: 'Description',
        content: <DescriptionView item={item} />,
      },
      {
        title: 'Access',
        content: <AccessView item={item} />,
      },
    ]

    const videoLinks = item.links.filter(
      link => link.linkProtocol === 'video:youtube'
    )
    if (videoLinks.length > 0) {
      tabData.push({
        title: videoLinks.length === 1 ? 'Video' : 'Videos',
        content: <VideoView links={videoLinks} />,
      })
    }

    return (
      <div style={styleCenterContent}>
        <div style={styleDetailWrapper}>
          <h1 style={styleTitle}>{item.title}</h1>
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
