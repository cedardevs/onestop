import React, { Component } from 'react'
import ReactDOM from 'react-dom'

const styleContainer = {};

const styleGrid = {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    alignItems: 'flex-start',
    alignContent: 'flex-start',
    margin: "2em 0 0 2em"
};

const styleGridItemContainer = {
    width: "20em",
    height: "10em",
    color: "black",
    backgroundColor: "white",
    margin: "0 2em 2em 0"

};

const styleGridItem = {};

export default class CollectionGrid extends Component {

    constructor(props) {
        super(props)
        this.renderShowMoreButton = this.renderShowMoreButton.bind(this)
    }

    renderShowMoreButton() {
        if(this.props.returnedHits < this.props.totalHits) {
            return <div className={styles.buttonContainer}>
                <button className={`pure-button ${styles.button}`} onClick={() => this.props.fetchMoreResults()}>Show More Results</button>
            </div>
        }
    }

    componentDidUpdate() {
        const focusCard = ReactDOM.findDOMNode(this.focusCard)
        if (_.isNull(focusCard)) {
            ReactDOM.findDOMNode(this.resultCount).focus()
        } else {
            focusCard.focus()
        }
    }


    render() {
        let flexItems = [];
        for(let n = 1; n < 100; n++) {
            flexItems.push(
                <div key={n} style={styleGridItemContainer}>
                    <div style={styleGridItem}>Grid Item {n}</div>
                </div>
            );
        }
        return (
            <div style={styleContainer}>
                <div style={styleGrid}>
                    {flexItems}
                </div>
            </div>
        );
    }
}