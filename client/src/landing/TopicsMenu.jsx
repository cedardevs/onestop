import React from 'react'

const styleTopicsMenu = {
  display: 'flex',
  flexWrap: 'wrap',
  justifyContent: 'center',
  margin: 0,
  padding: 0
}

const styleTopic = {
  padding: '1em'
}

const styleTopicButton = {
  background: 'none',
  display: 'block',
  fontWeight: 'bold,',
  fontSize: '1.17em',
  border: 'none',
  textAlign: 'center',
  verticalAlign: 'middle',
  textDecoration: 'none',
}

const styleTopicImage = {
  width: '5em',
  height: '5em',
  maxWidth: '100%',
  transition: 'transform 200ms'
}

class TopicsMenu extends React.Component {

  search = query => {
    const { submit, updateQuery } = this.props
    updateQuery(query)
    submit(query)
  }

  render() {

    let topics = [
      {
        title: 'Weather',
        term: 'weather',
        icon: require('../../img/topics/weather.png'),
      },
      {
        title: 'Climate',
        term: 'climate',
        icon: require('../../img/topics/climate.png'),
      },
      {
        title: 'Satellites',
        term: 'satellite',
        icon: require('../../img/topics/satellites.png'),
      },
      {
        title: 'Fisheries',
        term: 'fisheries',
        icon: require('../../img/topics/fisheries.png'),
      },
      {
        title: 'Coasts',
        term: 'coasts',
        icon: require('../../img/topics/coasts.png'),
      },
      {
        title: 'Oceans',
        term: 'oceans',
        icon: require('../../img/topics/oceans.png'),
      },
    ]
    topics = topics.map((topic, i) => {
      return (
          <div
              style={styleTopic}
              key={i}
              onClick={() => this.search(topic.term)}
          >
            <button style={styleTopicButton}>
              <img style={styleTopicImage} src={topic.icon} alt={topic.title} aria-hidden="true" />
              <div>{topic.title}</div>
            </button>
          </div>
      )
    })
    return <ul style={styleTopicsMenu}>{topics}</ul>
  }
}

export default TopicsMenu
