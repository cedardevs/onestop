import React, { PropTypes } from 'react'

const ResultsList = ({opened, toggleVisibility}) => {
    const FACET_MENU_WIDTH = 270;
    const TRANSITION = '.3s ease-out';

    const styles = {
        topContainer: {
            backgroundColor: 'blue',
            transition: TRANSITION,
            WebkitTransition: '-webkit-transform '+TRANSITION,

            transform: opened ? '' : 'translate(-'+FACET_MENU_WIDTH+'px)',
            WebkitTransform: opened ? '' : 'translate(-'+FACET_MENU_WIDTH+'px)', /* Safari */
            MsTransform: opened ? '' : 'translate(-'+FACET_MENU_WIDTH+'px)', /* IE 9 */
            willChange: 'transform',
        },
        handle: {
            position: 'absolute',
            top: '0',
            left: FACET_MENU_WIDTH + 'px',
            width: '20px',
            height: '20px',
            border: '1px ridge black',
            backgroundColor: '#3498DB',
            color: 'white',
            textAlign: 'center',
            fontSize: '15px',
        },
        content: {
            position: 'absolute',
            top: '0',
            left: '0',
            width: FACET_MENU_WIDTH+'px',
            height: '400px',
            backgroundColor: '#dddddd',
            boxShadow: '0px 5px 8px 0px rgba(0,0,0,0.2)',
            overflowY: 'auto',
        }

    };

    const handleClicking = () => toggleVisibility();

    return <div style={styles.topContainer}>
        <div style={styles.content}>
        </div>
        <div style={styles.handle}
             onclick={handleClicking}
             onTouchTap={handleClicking}
             onHover={handleClicking}>
            <b>=</b>
        </div>
    </div>
};

ResultsList.propTypes = {
    results: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired
    })).isRequired,
    loading: PropTypes.bool.isRequired
};

ResultsList.defaultProps = {loading: false, results: []};

export default ResultsList
