import React, {Component} from 'react'

import ContextMenu from './menus/ContextMenu'
import FlexColumn from '../common/FlexColumn'
import Header from './Header'
import Content from './Content'
import Footer from './Footer'

const defaultPadding = '1em'

const initialContextMenu = {
    visible: false,
    x: 0,
    y: 0,
    width: 200,
    height: 300
}

const styleContainer = {
    minHeight: '100vh',
    width: '100vw',
    overflow: 'hidden',
    // userSelect: 'none',
}

export default class Container extends Component {

    constructor(props) {
        super(props);

        // initial/default state
        this.state = { contextMenu: initialContextMenu };

        // bind events to component
        this.handleContextMenuShow = this.handleContextMenuShow.bind(this);
        this.handleContextMenuHide = this.handleContextMenuHide.bind(this);
    }

    // mouse events
    handleContextMenuShow(event) {
        event.preventDefault()
        this.setState( { contextMenu: { visible: true, x: event.pageX, y: event.pageY, width: initialContextMenu.width, height: initialContextMenu.height } });
        this.forceUpdate()
    }

    handleContextMenuHide() {
        this.setState({ contextMenu: { visible: false } });
    }

    render() {
        const styles = Object.assign({}, styleContainer, this.props.style)
        return (
            <div
                /* mouse events - right-click menu */
                onContextMenu={this.handleContextMenuShow} /* right click */
            >
                <FlexColumn
                    items={[
                        <Header content={this.props.header} padding={defaultPadding} key={"header"}/>,
                        <Content
                            left={this.props.left}
                            leftWidth={this.props.leftWidth}
                            leftVisible={this.props.leftVisible}

                            tabs={this.props.tabs}
                            tabCurrent={this.props.tabCurrent}
                            onTabChange={this.props.onTabChange}

                            middle={this.props.middle}

                            right={this.props.right}
                            rightWidth={this.props.rightWidth}
                            rightVisible={this.props.rightVisible}
                            padding={defaultPadding}
                            key={"content"}
                        />,
                        <Footer content={this.props.footer} padding={defaultPadding} key={"footer"}/>
                    ]}
                    style={styles}
                />
                <ContextMenu
                    content={this.props.menu}
                    visible={this.state.contextMenu.visible}
                    onHide={this.handleContextMenuHide}
                    x={this.state.contextMenu.x}
                    y={this.state.contextMenu.y}
                    width={this.state.contextMenu.width}
                    height={this.state.contextMenu.height}
                    padding="1em"
                />
            </div>
        )
    }
}