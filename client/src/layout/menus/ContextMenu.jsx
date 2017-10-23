import React, { Component } from 'react'

const styleContextMenuVisible = (x, y, width, height) => {

    // get window dimensions
    const maxX = window.innerWidth
    const maxY = window.innerHeight

    // keep menu in bounds
    let left = (x+width) > maxX ? maxX - width : x
    let top = (y+height) > maxY ? maxY - height : y

    return {
        left: left + 'px',
        top: top + 'px',
        width: width + 'px',
        height: height + 'px',
        position: 'absolute',
        backgroundColor: 'rgba(0, 0, 0, 0.99)',
        color: '#FFF',
        opacity: 0.67,
        borderRadius: '10px',
        display: 'block',
        userSelect: 'none'
    }
}

const styleContextMenuHidden = () => {
    return {
        display: 'none'
    }
}

export default class ContextMenu extends Component {

    constructor(props) {
        super(props);

        this.state = { wrapperRef: this };

        this.setWrapperRef = this.setWrapperRef.bind(this);
        this.handleClickOutside = this.handleClickOutside.bind(this);
    }

    componentDidMount() {
        document.addEventListener('mousedown', this.handleClickOutside);
    }

    componentWillUnmount() {
        document.removeEventListener('mousedown', this.handleClickOutside);
    }

    setWrapperRef(node) {
        this.state.wrapperRef = node;
    }

    handleClickOutside(event) {
        if (this.state.wrapperRef && !this.state.wrapperRef.contains(event.target) && this.props.visible) {
            this.props.onHide()
        }
    }

    render() {
        const styleVisible = styleContextMenuVisible(this.props.x, this.props.y, this.props.width, this.props.height)
        const styleHidden = styleContextMenuHidden()
        const styles = Object.assign({}, this.props.visible ? styleVisible : styleHidden, { padding: this.props.padding })

        return (
            <div ref={this.setWrapperRef} style={styles}>
                { this.props.content }
            </div>
        )
    }
}