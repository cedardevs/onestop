import React from 'react'
import TopicsMenuButton from './TopicsMenuButton'
import {fontFamilySerif} from '../../utils/styleUtils'

const styleTopics = {
  marginTop: '2.618em',
}

const styleTopicsLabel = {
  textAlign: 'center',
  fontFamily: fontFamilySerif(),
  margin: '0 0 0.618em 0',
}

const styleTopicsMenu = {
  display: 'flex',
  flexWrap: 'wrap',
  justifyContent: 'center',
  listStyle: 'none',
  margin: 0,
  padding: 0,
}

class TopicsMenu extends React.Component {
  search = query => {
    const {submit} = this.props
    submit(query)
  }

  render() {
    let topics = [
      {
        title: 'Weather',
        term: 'weather',
        icon: require('../../../img/topics/weather.png'),
      },
      {
        title: 'Climate',
        term: 'climate',
        icon: require('../../../img/topics/climate.png'),
      },
      {
        title: 'Satellites',
        term: 'satellite',
        icon: require('../../../img/topics/satellites.png'),
      },
      {
        title: 'Fisheries',
        term: 'fisheries',
        icon: require('../../../img/topics/fisheries.png'),
      },
      {
        title: 'Coasts',
        term: 'coasts',
        icon: require('../../../img/topics/coasts.png'),
      },
      {
        title: 'Oceans',
        term: 'oceans',
        icon: require('../../../img/topics/oceans.png'),
      },
    ]
    topics = topics.map((topic, i) => {
      return (
        <li key={i}>
          <TopicsMenuButton key={i} topic={topic} onClick={this.search} />
        </li>
      )
    })
    return (
      <nav style={styleTopics} aria-labelledby="popularTopics">
        <h2 style={styleTopicsLabel} id="popularTopics">
          Explore Popular Topics
        </h2>
        <ul style={styleTopicsMenu}>{topics}</ul>
      </nav>
    )
  }
}

export default TopicsMenu
