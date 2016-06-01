import React, { PropTypes } from 'react'
import List from '../../node_modules/material-ui/lib/lists/list';
import ListItem from '../../node_modules/material-ui/lib/lists/list-item';

const ResultsList = ({opened, toggleVisibility}) => {
    const styles = {

        content: {
            display: opened ? 'block' : 'none',
            position: 'absolute',
            backgroundColor: 'grey',
            width: '160px',
            boxShadow: '0px 8px 16px 0px rgba(0,0,0,0.2)', 
            padding: '12px 16px',
        }
    };

    const handleClicking = () => toggleVisibility();

    return <div>
        <p onTouchTap={handleClicking}>Click me, Opened={opened ? 'true' : 'false'}</p>
        {opened}
        <div style={styles.content}>
            This is a test.
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
