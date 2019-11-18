import React from 'react'

import Background from './Background'
import FlexColumn from '../common/ui/FlexColumn'
import Header from './Header'
import Banner from './Banner'
import Content from './Content'
import Footer from './Footer'
import Disclaimer from './Disclaimer'
import HiddenAccessibilityHeading from './HiddenAccessibilityHeading'

const defaultPadding = '1em'

const styleContainer = {
  minHeight: '100vh',
  width: '100%',
  overflow: 'hidden',
}

export default class Layout extends React.Component {
  render() {
    const {
      style,
      disclaimer,
      header,
      bannerGraphic,
      bannerHeight,
      bannerArcHeight,
      bannerVisible,
      hiddenAccessibilityHeading,
      left,
      leftWidth,
      leftOpen,
      leftVisible,
      leftStyle,
      middle,
      middleMaxWidth,
      right,
      rightWidth,
      rightOpen,
      rightVisible,
      footer,
    } = this.props

    const styles = Object.assign({}, styleContainer, style)

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
            <HiddenAccessibilityHeading
              content={hiddenAccessibilityHeading}
              key={'hiddenAccessibilityHeading'}
            />,
            <Content
              left={left}
              leftWidth={leftWidth}
              leftOpen={leftOpen}
              leftVisible={leftVisible}
              leftStyle={leftStyle}
              middle={middle}
              middleMaxWidth={middleMaxWidth}
              right={right}
              rightWidth={rightWidth}
              rightOpen={rightOpen}
              rightVisible={rightVisible}
              padding={defaultPadding}
              key={'content'}
            />,
            <Footer content={footer} padding={defaultPadding} key={'footer'} />,
          ]}
          style={styles}
        />
      </Background>
    )
  }
}
