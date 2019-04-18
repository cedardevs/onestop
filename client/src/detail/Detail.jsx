import React, {Component} from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import DescriptionView from './DescriptionView'
import OverviewView from './OverviewView'
import AccessView from './AccessView'
import VideoView from './VideoView'
import Tabs from './Tabs'
import {boxShadow} from '../common/defaultStyles'
import {fontFamilySerif} from '../utils/styleUtils'
import {identifyProtocol} from '../utils/resultUtils'

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

const styleFocusDefault = {
  outline: '2px dashed #5C87AC',
}

//-- Component
class Detail extends Component {
  constructor(props) {
    super(props)
    this.state = {
      focusing: false,
      shouldFocusHeader: true,
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState(prevState => {
      return {
        ...prevState,
        shouldFocusHeader: true,
      }
    })
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.headerRef && this.state.shouldFocusHeader) {
      ReactDOM.findDOMNode(this.headerRef).focus()
    }
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
        shouldFocusHeader: false,
      }
    })
  }

  render() {
    const {
      id,
      item,
      loading,
      totalGranuleCount,
      navigateToGranules,
    } = this.props

    if (loading) {
      return (
        <div style={styleCenterContent}>
          <div style={styleDetailWrapper}>
            <h1 style={styleLoadingMessage}>Loading...</h1>
          </div>
        </div>
      )
    }

    if (!item) {
      // TODO error style? actually report an error in the flow if the collection is not found when search returns?
      return (
        <div style={styleCenterContent}>
          <div style={styleDetailWrapper}>
            <h1 style={styleErrorMessage}>
              There was a problem loading your collection.
            </h1>
          </div>
        </div>
      )
    }

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

    const styleFocused = {
      ...(this.state.focusing ? styleFocusDefault : {}),
    }

    const styleHeadingSpanApplied = {
      ...styleHeadingSpan,
      ...styleFocused,
    }

    return (
      <div style={styleCenterContent}>
        <div style={styleDetailWrapper}>
          <h1
            key="filtersH1"
            tabIndex={-1}
            ref={header => {
              this.headerRef = header
            }}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
            style={styleTitle}
          >
            <div style={styleHeadingSpanApplied}>{item.title}</div>
          </h1>
          <DescriptionView
            item={item}
            itemUuid={id}
            totalGranuleCount={totalGranuleCount}
            navigateToGranules={() => navigateToGranules(id)}
          />
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
