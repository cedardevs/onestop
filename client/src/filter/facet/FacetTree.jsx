import React, { Component } from 'react'
import Facet from './Facet'
import _ from 'lodash'

/**
  This component contains the content of a facet category. It is essentially a
  speciallized tree menu.
**/

const styleFacet = backgroundColor => {
  return {
    padding: '0.618em',
    backgroundColor: backgroundColor ? backgroundColor : 'initial',
    color: '#FFF',
    display: 'flex',
    textAlign: 'left',
    alignItems: 'center',
  }
}

const styleRovingFocus = {
  boxShadow: '0 0 0 1px #FFF',
}

const styleRovingFocusCheckbox = {
  outline: 'none',
  boxShadow: '0 0 2px 2px #12347C',
}


const styleExpandableContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
  }
}

export default class FacetTree extends Component {
  constructor(props) {
    super(props)

    this.state = {
      facetMap: {},
      allFacetsInOrder: [],
      facetLookup: {},
    }
  }

  componentWillReceiveProps(nextProps) {
    var self = this
    self.setState(prevState => {
      return {
        ...prevState,
         // reset facet references before reparsing the map, which should populate them again
        allFacetsInOrder: [],
        facetLookup: {},
      }
    })
    if(!_.isEqual({}, self.props.facetMap)) {
      // parse map with the top layer being marked as visible: true.
      // note this function uses side effects to alter the facetMap itself,
      // then we simply store that in state
      self.parseMap(self.props.facetMap, 1)
      self.setState(prevState => {
        let facetMap = Object.assign({}, prevState.facetMap)
        facetMap = self.props.facetMap
        return {
          ...prevState,
          facetMap: facetMap,
        }
      })
    }
  }

  handleExpandableToggle = event => {
    var self = this
    this.setState(prevState => {

      var node = this.state.facetLookup[event.value]
      if(node) {
        node.open = event.open
        var updateVisibility = (children, visibility) => {
          var updateVisibilityFunc = (value, key) => {
            value.visible = visibility
            updateVisibility(value.children, visibility && value.open)
          }
          _.each(children, updateVisibilityFunc)
        }

        updateVisibility(node.children, node.open)

        return {
          ...prevState,
          facetMap: this.state.facetMap, // editing to node directly modified this, this is to trigger the rest of the stack
        }
      }
    })
  }

  updateRovingIndex = (facetId) => {
    var self = this
    this.setState(prevState => {

      // var oldIndex = this.state.facetLookup(self.state.rovingIndex)
      const oldIndex = prevState.facetLookup[prevState.rovingIndex]
      oldIndex.tabIndex = '-1'

      var newIndex = prevState.facetLookup[facetId]
      newIndex.tabIndex = '0'

      return {
        ...prevState,
        facetMap: this.state.facetMap, // editing to node directly modified this, this is to trigger the rest of the stack
        rovingIndex: facetId,
      }
    })
    document.getElementById(facetId).focus()
  }

  triggerRight = () => {
    const id = this.state.rovingIndex
    var node = this.state.facetLookup[id]
    if (!node.open) {
      // open node
      this.handleExpandableToggle({open: true, value: id})
    }
    else {
      // move focus to first child
      var facetId = node.relations.children[0]
      if(facetId) {
        this.updateRovingIndex(facetId)
      }
    }
  }

  triggerLeft = () => {
    const id = this.state.rovingIndex
    var node = this.state.facetLookup[id]
    if (node.open) {
      // close node
      this.handleExpandableToggle({open: false, value: id})
    }
    else {
      // move focus to parent
      var facetId = node.relations.parent
      if(facetId) {
        this.updateRovingIndex(facetId)
      }
    }
  }

  moveFocusDown = () => {
    var self = this // scope
    const id = this.state.rovingIndex
    const orderIndex = _.indexOf(this.state.allFacetsInOrder, id)

    if (orderIndex < this.state.allFacetsInOrder.length - 1) {
      const nextVisible = _.find(this.state.allFacetsInOrder, function(facetId) {

        var node = self.state.facetLookup[facetId]
        return node.visible

      }, orderIndex+1)

      if(nextVisible) {
        this.updateRovingIndex(nextVisible)
      }
    }
  }

  moveFocusToEnd = () => {
    var self = this // scope
    const nextVisible = _.findLast(this.state.allFacetsInOrder, function(facetId) {

      var node = self.state.facetLookup[facetId]
      return node.visible

    })

    if(nextVisible) {
      this.updateRovingIndex(nextVisible)
    }
  }

  moveFocusToStart = () => {
    this.updateRovingIndex(this.state.allFacetsInOrder[0])
  }

  moveFocusUp = () => {
    var self = this // scope
    const id = this.state.rovingIndex
    const orderIndex = _.indexOf(this.state.allFacetsInOrder, id)
    if (orderIndex > 0) {
      const nextVisible = _.findLast(this.state.allFacetsInOrder, function(facetId) {

        var node = self.state.facetLookup[facetId]
        return node.visible

      }, orderIndex-1)

      this.updateRovingIndex(nextVisible)
    }
  }

  isSelected = (category, term) => {
    const selectedTerms = this.props.selectedFacets[category]
    if (!selectedTerms) {
      return false
    }
    else {
      return selectedTerms.includes(term)
    }
  }

  handleSelectToggleMouse = (e) => {
    this.props.handleSelectToggle(e.value, e.checked)
  }

  createFacetComponent = (facet, parent, siblings) => {
    // handle any nulls that might get into this function
    let facetComponent = null
    if (!facet) {
      return facetComponent
    }

    if ('children' in facet) {
      const hasChildren = !_.isEmpty(facet.children)
      const children = hasChildren? this.createFacetComponent(facet.children, facet):null

      facetComponent = (
          <Facet
            facetId={facet.id}
            key={facet.id}

            category={facet.category}
            term={facet.term}
            count={facet.count}

            open={facet.open}
            selected={this.isSelected(facet.category, facet.term)}

            tabIndex={facet.tabIndex}
            focused={this.state.focus}

            children={children}
            hasChildren={hasChildren}

            handleSelectToggleMouse={this.handleSelectToggleMouse}
            handleExpandableToggle={this.handleExpandableToggle}

            styleFacet={styleFacet(this.props.backgroundColor)}
            styleFocus={styleRovingFocus}
            styleCheckboxFocus={styleRovingFocusCheckbox}
            styleChildren={styleExpandableContent(this.props.marginNest)}
            />
      )
    } else {
      // for each key recurse
      let facetComponents = []
      Object.keys(facet).forEach(subFacet => {
        facetComponents.push(this.createFacetComponent(facet[subFacet]))
      })
      return (facetComponents)
    }
    return facetComponent
  }

  parseMap = (map, level, parentOpen, parentId) => {
    if(_.isEqual({}, map)) {
      // cannot parse empty map
      return []
    }

    _.each(map, function (value,key) {
      const keyParts = _.concat(_.words(value.category), _.words(value.term))
      value.relations = {}
      value.open = false // always default to everything collapsed
      value.tabIndex = '-1'
    })

    if (level == 1) { // id first layer to set the initial tab focus
      const value = _.map(map, function(value,key) {
        return value
      })[0]
      value.tabIndex = '0'
      this.setState(prevState => {
        return {
          ...prevState,
          rovingIndex: value.id,
        }
      })
    }

    const siblings = _.map(map, function(value, key) {
      return value.id
    })

    var self = this // scoping fix
    _.each(map, function (value,key) {

      self.setState(prevState => {

        // update state that lets us quickly traverse the nodes in up/down order
        let allFacetsInOrder = Object.assign([], prevState.allFacetsInOrder)
        allFacetsInOrder.push(value.id)

        // update state that lets us set focus to another node or update visibility, since it is a property that combines the state of several nodes
        let facetLookup = Object.assign({}, prevState.facetLookup)
        facetLookup[value.id] = value

        return {
          ...prevState,
          allFacetsInOrder: allFacetsInOrder,
          facetLookup: facetLookup,
        }
      })

      value.relations.parent = parentId
      value.relations.children = self.parseMap(value.children, level+1, parentOpen && value.open, value.id )

      value.visible = (level==1) || !!parentOpen
    })

    return siblings
  }

  handleKeyPressed = (e) => {
    // do nothing if modifiers are pressed
    if( e.metaKey || e.shiftKey || e.ctrlKey || e.altKey ) return

    e.stopPropagation()

    if (e.keyCode == 32 || e.keyCode == 13) { // space and enter
      e.preventDefault() // prevent scrolling down on space press
      const {facetId, category, term} = this.state.facetLookup[this.state.rovingIndex]
      const selected = this.isSelected(category, term)
      this.props.handleSelectToggle({id: facetId, category: category, term: term}, !selected)
    }
    if (e.keyCode == 36) { // home
      this.moveFocusToStart()
    }
    if (e.keyCode == 35) { // end
      this.moveFocusToEnd()
    }
    if (e.keyCode == 38) { // up
      e.preventDefault() // prevent scrolling behavior
      this.moveFocusUp()
    }
    if (e.keyCode == 40) { // down
      e.preventDefault() // prevent scrolling behavior
      this.moveFocusDown()
    }
    if (e.keyCode == 37) { // left
      this.triggerLeft()
    }
    if (e.keyCode == 39) { // right
      this.triggerRight()
    }
  }

  handleKeyDown = (e) => {
    // prevent the default behavior for tree control keys
    // these are the control keys used by the tree menu
    if ( !e.metaKey && !e.shiftKey && !e.ctrlKey && !e.altKey && [13,32,35,36,37,38,39,40].includes(e.keyCode) ) {
      e.preventDefault()
    }
  }

  render() {
    const facetHierarchy = this.createFacetComponent(this.state.facetMap)

    return (
      <div

        role='tree'
        aria-labelledby={this.props.headerId}
        aria-multiselectable='true'

        onKeyUp={this.handleKeyPressed} // onKeyDown isnt an actual keypress
        onKeyDown={this.handleKeyDown}

        >
        {facetHierarchy}
      </div>
    )
  }
}
