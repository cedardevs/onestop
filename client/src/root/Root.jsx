import React from 'react'
import BannerContainer from './banner/BannerContainer'
import HeaderContainer from './HeaderContainer'
import DetailContainer from '../detail/DetailContainer'
import LoadingContainer from '../loading/LoadingContainer'
import Footer from './Footer.jsx'
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
        <HeaderContainer showSearch={this.showSearch()} showMenu={this.showMenu()}/>
        <div className={styles.main}>
          <LoadingContainer/>
          {this.props.children}
        </div>
      </div>
      <div className={styles.footer}>
        <Footer/>
     </div>
   </div>
  }

  showSearch() {
    return this.location !== '/' && this.location.indexOf('508') === -1
  }

  showMenu() {
    return this.location === '/'
  }
}


export default RootComponent
