import React, { PropTypes } from 'react'
import Detail from './DetailComponent.jsx'

const DetailList = ({details, onCardClick}) => {
    console.dir(details);
    return (<ul>
        <Detail
            key={details.id}
            {...details.details}
            flipped={details.flipped}
            onClick={() => onCardClick(details.id)}
        />
    </ul>);

}

DetailList.propTypes = {
    details: PropTypes.shape({
        id: PropTypes.string.isRequired,
        title: PropTypes.string.isRequired,
        summary: PropTypes.string.isRequired,
        links: PropTypes.arrayOf(PropTypes.shape({
            href: PropTypes.string.isRequired,
            type: PropTypes.string.isRequired
        })).isRequired
    }).isRequired,
    flipped: PropTypes.bool.isRequired,
    onCardClick: PropTypes.func.isRequired
}

DetailList.defaultProps = {
    id: '',
    details: {
        id: '',
        title: '',
        summary: '',
        links: []
    },
    flipped: false
}

export default DetailList