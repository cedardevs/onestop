import React from 'react'
import BannerContainer from './banner/BannerContainer'
import HeaderContainer from './HeaderContainer'
import FooterContainer from './FooterContainer'
import InfoContainer from '../common/info/infoContainer'
import DetailContainer from '../detail/DetailContainer'
import LoadingContainer from '../loading/LoadingContainer'
import styles from './root.css'

class RootComponent extends React.Component {
  constructor(props) {
    super(props)

    this.location = props.location.pathname
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
  }

  render() {
    return <div className={styles.rootContainer}>
      <div className={styles.mainContent}>
        <BannerContainer/>
        <DetailContainer/>
        <HeaderContainer showSearch={this.isNotLanding() && this.isNot508()}
           homeUrl={this.homeUrl()}/>
        <div className={styles.main}>
          <InfoContainer modalMode={this.isNotLanding()}/>
          <LoadingContainer/>
          {this.props.children}
        </div>
      </div>
      <div className={styles.footer}>
        <FooterContainer/>
     </div>
   </div>
  }

  isNotLanding() {
    return this.location !== '/' && this.location !== '/508/'
  }

  isNot508() {
    return this.location.indexOf('508') === -1
  }

  homeUrl() {
    const { host, pathname } = location
    return `//${host}${pathname ? pathname : '/'}#/${this.isNot508() ? '' : '508/'}`
  }
}


export default RootComponent
