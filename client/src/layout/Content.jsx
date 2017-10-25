import React, {Component} from 'react'
import FlexRow from '../common/FlexRow'
import Left from './Left'
import Middle from './Middle'
import Right from './Right'

const style = {
    display: 'flex',
    flex: '1 1 auto',
    position: 'relative',
    justifyContent: 'space-between',
    alignItems: 'stretch',
    width: '100%'
}

export default class Content extends Component {
    render() {
        const styles = Object.assign({}, style, this.props.style)
        return (
            <FlexRow
                items={[
                    this.props.left ? <Left content={this.props.left} width={this.props.leftWidth} padding={this.props.padding} visible={this.props.leftVisible} key={"left"}/> : null,
                    <Middle content={this.props.middle} tabs={this.props.tabs} currentTab={this.props.tabCurrent} onTabChange={this.props.onTabChange} padding={this.props.padding} key={"middle"}/>,
                    this.props.right ? <Right content={this.props.right} width={this.props.rightWidth} padding={this.props.padding} visible={this.props.rightVisible} key={"right"}/> : null
                ]}
                style={styles}
            />
        )
    }
}