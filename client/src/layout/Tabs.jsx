import React, { Component } from 'react'
import FlexRow from '../common/FlexRow'
import TabButton from './TabButton'

const styleTabs = {
    flexWrap: 'nowrap',
    flexShrink: 0,
    position: 'sticky',
    top:'0',
    width: '100%',
    justifyContent: 'space-between'
}

export default class Tabs extends Component {
    constructor(props) {
        super(props);

        // bind events to component
        this.handleTabChange = this.handleTabChange.bind(this);
    }

    handleTabChange(event) {
        const tabValue = event.currentTarget.value
        if(this.props.onChange) {
            this.props.onChange(tabValue)
        }
    }

    render() {
        let tabButtons = []
        if (this.props.titles) {
            this.props.titles.forEach((title: string) => {
                let active = false
                if(this.props.activeTitle && this.props.activeTitle === title) {
                    active = true
                }

                tabButtons.push(
                    <TabButton
                        key={ title }
                        title={ title }
                        active={ active }
                        onChange={ this.props.onChange }
                        padding={ this.props.padding }
                    />
                )
            })
        }

        return (
            <FlexRow items={ tabButtons } style={ styleTabs }/>
        )
    }
}