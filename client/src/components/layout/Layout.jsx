import React from 'react'

import Background from './Background'
import FlexColumn from '../common/ui/FlexColumn'
import Header from './Header'
import Banner from './Banner'
import Content from './Content'
import Footer from './Footer'
import Disclaimer from './Disclaimer'
import FlexRow from '../common/ui/FlexRow'
import {fontFamilySerif} from '../../utils/styleUtils'

const defaultPadding = '1em'

const styleContainer = {
  minHeight: '100vh',
  width: '100%',
  overflow: 'hidden',
}

const styleTitleRow = {
  justifyContent: 'flex-start',
  padding: '0.618em 0 0.618em 0',
  margin: 0,
  backgroundColor: '#222C37',
  fontFamily: fontFamilySerif(),
}

const styleTitle = {
  fontSize: '1.2em',
  padding: '0 0 0 18em',
  margin: 0,
}

export default class Layout extends React.Component {
  render() {
    const {
      location,
      style,
      disclaimer,
      header,
      bannerGraphic,
      bannerHeight,
      bannerArcHeight,
      bannerVisible,
      left,
      leftWidth,
      leftOpen,
      leftVisible,
      middle,
      middleMaxWidth,
      right,
      rightWidth,
      rightOpen,
      rightVisible,
      footer,
    } = this.props

    const styles = Object.assign({}, styleContainer, style)

    const searchResultPageTitle =
      location.pathname && location.pathname.includes('collections')
        ? 'Collection search results'
        : location.pathname && location.pathname.includes('granules')
          ? 'Granule search results'
          : null

    const titleRow = searchResultPageTitle ? (
        <FlexColumn items={[ <FlexRow
            style={styleTitleRow}
            key='result-page-title'
            items={[ <h1 key='result-title-row' style={styleTitle}>{searchResultPageTitle}</h1> ]}
        /> ]} />
    ) : null

    return (
      <Background>
        <FlexColumn
          items={[
            <Disclaimer content={disclaimer} key={'disclaimer'} />,
            <Header content={header} padding={defaultPadding} key={'header'} />,
            <Banner
              graphic={bannerGraphic}
              height={bannerHeight}
              arcHeight={bannerArcHeight}
              visible={bannerVisible}
              key={'banner'}
            />,
            <FlexColumn key={'title'} items={[ titleRow ]} />,
            <Content
              left={left}
              leftWidth={leftWidth}
              leftOpen={leftOpen}
              leftVisible={leftVisible}
              middle={middle}
              middleMaxWidth={middleMaxWidth}
              right={right}
              rightWidth={rightWidth}
              rightOpen={rightOpen}
              rightVisible={rightVisible}
              padding={defaultPadding}
              key={'content'}
              style={{
                position: 'relative',
                zIndex: 1,
                margin: '-1.618em 0 0 0',
              }}
            />,
            <Footer content={footer} padding={defaultPadding} key={'footer'} />,
          ]}
          style={styles}
        />
      </Background>
    )
  }
}
