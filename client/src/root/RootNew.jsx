import React, { Component } from 'react'

// import { style, cssRaw } from 'typestyle'
// import { normalize, setupPage } from 'csstips'

import FlexColumn from '../common/FlexColumn'
import Checkbox from '../common/input/Checkbox'

import ContextMenuItem from '../layout/menus/ContextMenuItem'
import Container from '../layout/Container'

import BannerContainer from './banner/BannerContainer'
import DetailContainer from '../detail/DetailContainer'
import HeaderContainer from './HeaderContainer'

import Filters from '../filter/Filters'

import InfoContainer from '../common/info/infoContainer'
import LoadingContainer from '../loading/LoadingContainer'

import FooterContainer from './FooterContainer'




// normalize();
// setupPage('#root');

/** Import the file */
// cssRaw(`
// body {
//   overflow: 'hidden';
// }
// `);

// component
export default class Root extends Component {

    constructor(props) {
        super(props);

        // bind events to component
        this.handleLeftVisibility = this.handleLeftVisibility.bind(this);
        this.handleRightVisibility = this.handleRightVisibility.bind(this);
        this.handleTabChange = this.handleTabChange.bind(this);


        this.hasUnsupportedFeatures = this.hasUnsupportedFeatures.bind(this)

        this.location = props.location.pathname

        this.state = { leftVisible: true, rightVisible: false, tabCurrent: "Entities", browserWarning: this.hasUnsupportedFeatures() };

    }

    componentWillUpdate(nextProps) {
        this.location = nextProps.location.pathname
    }

    handleLeftVisibility(event) {
        this.state.leftVisible = event.checked
        this.forceUpdate()
    }

    handleRightVisibility(event) {
        this.state.rightVisible = event.checked
        this.forceUpdate()
    }

    handleTabChange(to:string) {
        this.state.tabCurrent = to
        this.forceUpdate()
    }

    hasUnsupportedFeatures(){
        let browserWarning = false
        const htmlClasses = document.documentElement.className.split(' ')
        htmlClasses.forEach(htmlClass => {
            if(htmlClass.startsWith('no-')){ browserWarning = true; return; }
        })
        return browserWarning
    }

    unsupportedBrowserWarning() {
        const wikiUrl = 'https://github.com/cedardevs/onestop/wiki/OneStop-Client-Supported-Browsers'
        return <aside role='alert' className={styles.browserWarning}>
        <span className={styles.close}
              onClick={()=>{this.setState({browserWarning: false})}}>x</span>
            <p>
                The browser that you are using to view this page is not currently supported.
                For a list of currently supported & tested browsers, please visit the
                <span> <a href={wikiUrl}>OneStop Documentation</a></span>
            </p>
        </aside>
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

    render() {
        const checkboxLeftVisible = <Checkbox
            checked={this.state.leftVisible}
            value="leftVisible"
            onChange={this.handleLeftVisibility}
        />

        const checkboxRightVisible = <Checkbox
            checked={this.state.rightVisible}
            value="rightVisible"
            onChange={this.handleRightVisibility}
        />

        const menu = <FlexColumn
            items={[
                <ContextMenuItem key="1" content={checkboxLeftVisible}/>,
                <ContextMenuItem key="2" content={checkboxRightVisible}/>
            ]}
        />

        const header = (
            <div>
                <BannerContainer/>
                <DetailContainer/>
                <HeaderContainer showSearch={this.isNotLanding() && this.isNot508()}
            homeUrl={this.homeUrl()}/>
                {this.state.browserWarning ? this.unsupportedBrowserWarning() : <div></div>}
            </div>
        )

        const middle = (
            <div>
                <InfoContainer modalMode={this.isNotLanding()}/>
                <LoadingContainer/>
                {this.props.children}
            </div>
        )

        let tabbedContent = new Map();
        tabbedContent.set("Entities", {
            left: <Filters/>,
            middle: middle,
            right: "Entities right"
        })
        // tabbedContent.set("Relationships", {
        //     left: "Relationships left",
        //     middle: "Relationships middle",
        //     right: "Relationships right"
        // })
        // tabbedContent.set("Endpoints", {
        //     left: "Endpoints left",
        //     middle: "Endpoints middle",
        //     right: "Endpoints right"
        // })

        // let d = new Date()
        // tabbedContent.set("Overview", {
        //     left: "Overview left",
        //     middle: "Overview middle ".repeat(500),
        //     right: d.toString()
        // })


        const content = tabbedContent.get(this.state.tabCurrent)
        return (
            <Container
                menu={ menu }
                header={ header }
                left={content.left}
                leftWidth={256}
                leftVisible={this.state.leftVisible}

                tabs={ Array.from( tabbedContent.keys() ) }
                tabCurrent={this.state.tabCurrent}
                onTabChange={this.handleTabChange}

                middle={content.middle}

                right={content.right}
                rightWidth={256}
                rightVisible={this.state.rightVisible}
                footer={<FooterContainer/>}
            />
        )
    }
}