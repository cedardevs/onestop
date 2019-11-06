import React from 'react'
import Facet from './Facet'
import _ from 'lodash'
import {Key} from '../../../utils/keyboardUtils'

import Immutable from 'seamless-immutable'
import {
  boxShadow,
  FilterColors,
  FilterStyles,
} from '../../../style/defaultStyles'

/**
  This component contains the content of a facet category. It is essentially a
  specialized tree menu.
**/

const styleFacet = disabled => {
  return {
    ...FilterStyles.LIGHT,
    ...(disabled ? {color: FilterColors.DISABLED_TEXT} : {}),
    ...{
      padding: '0.309em',
      display: 'flex',
      textAlign: 'left',
      alignItems: 'center',
      marginBottom: '1px',
      boxShadow: boxShadow,
      borderTop: `1px solid ${FilterColors.MEDIUM}`,
    },
  }
}

const styleRovingFocus = {
  boxShadow: `0 0 0 1px ${FilterColors.TEXT}`,
}

const styleRovingFocusCheckbox = {
  outline: 'none',
  boxShadow: `0 0 1px 1px ${FilterColors.DARK_EMPHASIS}`,
}

const styleExpandableContent = marginNest => {
  return {
    marginLeft: marginNest ? marginNest : '1em',
  }
}

const initialFacetState = Immutable({
  open: false,
  visible: false,
  tabIndex: -1,
})

export default class FacetTree extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      facetList: [],
      hierarchy: [],
      rovingIndex: null,
      treeId: null,
    }
  }

  mergeChildren = (list1, list2) => {
    // custom hierarchy list merging...
    let list = []
    _.each(list1, node => {
      list.push(node)
    })
    _.each(list2, node => {
      const i = _.findIndex(list, n => {
        return n.id === node.id
      })
      if (i >= 0) {
        list[i] = Immutable.merge(list[i], {
          children: this.mergeChildren(list[i].children, node.children),
        })
      }
      else {
        list.push(node)
      }
    })
    list = _.sortBy(list, [ 'id' ])
    list = _.map(list, node => {
      return Immutable.merge(node, {
        setSize: list.length,
        posInSet:
          1 +
          _.findIndex(list, n => {
            return node.id === n.id
          }),
      })
    })
    return list
  }

  updateAllVisibility = () => {
    this.setState(prevState => {
      const facets = prevState.facetList
      _.each(this.state.hierarchy, facetInMap => {
        const index = this.facetIndex(facetInMap.id)
        facets[index] = Immutable.merge(facets[index], {visible: true})
        this.mutateFacetVisibility(facetInMap.children, facets, index)
      })

      return {
        ...prevState,
        facetList: facets,
      }
    })
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (
      !_.isEqual(this.props.hierarchy, nextProps.hierarchy) ||
      !_.isEqual(this.props.facetMap, nextProps.facetMap)
    ) {
      this.setState(prevState => {
        const facets = _.map(this.state.facetList, facet => {
          let f = Immutable.merge(facet, {count: 0}) // set all facet counts to zero
          const updatedFacet = _.find(nextProps.facetMap, updatedFacet => {
            return facet.id === updatedFacet.id
          })
          if (updatedFacet) {
            // update facet with refreshed info (ie count) from results
            f = Immutable.merge(f, updatedFacet)
          }
          return f
        })

        // find any new facets which are not already included and add them
        _.each(nextProps.facetMap, facet => {
          const oldFacet = _.find(this.state.facetList, oldFacet => {
            return facet.id === oldFacet.id
          })
          if (!oldFacet) {
            facets.push(Immutable.merge(initialFacetState, facet))
          }
        })
        return {
          ...prevState,
          facetList: _.sortBy(facets, [ 'term' ]), // make sure facets are sorted
          hierarchy: this.mergeChildren(
            this.state.hierarchy,
            nextProps.hierarchy
          ),
        }
      }, this.updateAllVisibility)
    }
  }

  componentDidMount() {
    // init state
    this.setState(prevState => {
      const facets = _.map(this.props.facetMap, facet => {
        return Immutable.merge(facet, initialFacetState)
      })
      // The first facet should be the only focusable one, initially.
      facets[0] = Immutable.merge(facets[0], {tabIndex: 0})
      const firstFocused = facets[0]

      return {
        ...prevState,
        facetList: facets,
        hierarchy: this.props.hierarchy,
        treeId: `tree-${this.props.headerId}`,
        rovingIndex: firstFocused.id,
      }
    }, this.updateAllVisibility)
  }

  lookupFacet = id => {
    return _.find(this.state.facetList, facet => {
      return facet.id === id
    })
  }

  facetIndex = id => {
    return _.findIndex(this.state.facetList, facet => {
      return facet.id === id
    })
  }

  mutateFacetVisibility = (hierarchy, list, index) => {
    let updateVisibility = (children, visibility) => {
      _.each(children, (value, key) => {
        const i = this.facetIndex(value.id)
        list[i] = Immutable.merge(list[i], {visible: visibility})
        updateVisibility(value.children, visibility && list[i].open)
      })
    }
    updateVisibility(hierarchy, list[index].open)
  }

  updateNodeVisibility = (facetInMap, open) => {
    this.setState(prevState => {
      const facets = prevState.facetList
      const index = this.facetIndex(facetInMap.id)
      facets[index] = Immutable.merge(facets[index], {open: open})

      this.mutateFacetVisibility(facetInMap.children, facets, index)

      return {
        ...prevState,
        facetList: facets,
      }
    })
  }

  handleExpandableToggle = event => {
    this.updateNodeVisibility(event.value, event.open)
  }

  updateRovingIndex = facetId => {
    this.setState(prevState => {
      const oldIndex = this.facetIndex(this.state.rovingIndex)
      const newIndex = this.facetIndex(facetId)
      const facets = this.state.facetList
      facets[oldIndex] = Immutable.merge(facets[oldIndex], {tabIndex: -1})
      facets[newIndex] = Immutable.merge(facets[newIndex], {tabIndex: 0})
      return {
        ...prevState,
        facetList: facets,
        rovingIndex: facetId,
      }
    })

    document.getElementById(facetId).focus()
  }

  lookupHierarchy = (id, list) => {
    let result = null
    _.each(list, node => {
      if (result) return
      if (node.id === id) {
        result = node
        return
      }
      result = this.lookupHierarchy(id, node.children)
    })
    return result
  }

  triggerRight = () => {
    const id = this.state.rovingIndex
    const node = this.lookupFacet(id)
    const nodeInMap = this.lookupHierarchy(id, this.state.hierarchy)
    if (!node.open) {
      // open node
      this.handleExpandableToggle({open: true, value: nodeInMap})
    }
    else {
      // move focus to first child
      const facet = nodeInMap.children[0]
      if (facet) {
        this.updateRovingIndex(facet.id)
      }
    }
  }

  triggerLeft = () => {
    const id = this.state.rovingIndex
    const node = this.lookupFacet(id)
    const nodeInMap = this.lookupHierarchy(id, this.state.hierarchy)
    if (node.open) {
      // close node
      this.handleExpandableToggle({open: false, value: nodeInMap})
    }
    else {
      // move focus to parent
      const facetId = nodeInMap.parent
      if (facetId) {
        this.updateRovingIndex(facetId)
      }
    }
  }

  moveFocusDown = () => {
    const id = this.state.rovingIndex
    const orderIndex = this.facetIndex(id)

    if (orderIndex < this.state.facetList.length - 1) {
      const nextVisible = _.find(
        this.state.facetList,
        facet => {
          return facet.visible
        },
        orderIndex + 1
      )

      if (nextVisible) {
        this.updateRovingIndex(nextVisible.id)
      }
    }
  }

  moveFocusToEnd = () => {
    const nextVisible = _.findLast(this.state.facetList, facet => {
      return facet.visible
    })

    if (nextVisible) {
      this.updateRovingIndex(nextVisible.id)
    }
  }

  moveFocusToStart = () => {
    this.updateRovingIndex(this.state.facetList[0].id)
  }

  moveFocusUp = () => {
    const id = this.state.rovingIndex
    const orderIndex = this.facetIndex(id)

    if (orderIndex > 0) {
      const nextVisible = _.findLast(
        this.state.facetList,
        facet => {
          return facet.visible
        },
        orderIndex - 1
      )

      this.updateRovingIndex(nextVisible.id)
    }
  }

  handleSelectToggleMouse = e => {
    this.props.handleSelectToggle(e.value, e.checked)
  }

  isFacetDisabled = facet => {
    return facet.count === 0
  }

  createFacetComponent = facetInMap => {
    const facet = this.lookupFacet(facetInMap.id)
    let facetComponent = null
    if (!facet) {
      return facetComponent
    }
    const facetChildren = _.map(facetInMap.children, facet =>
      this.createFacetComponent(facet)
    )
    const hasChildren = !_.isEmpty(facetInMap.children)
    const children = hasChildren ? facetChildren : null

    return (
      <Facet
        facetId={facet.id}
        facetMap={facetInMap}
        key={facet.id}
        category={facet.category}
        term={facet.term}
        count={facet.count}
        open={facet.open}
        keyword={facet.keyword}
        selected={facet.selected}
        disabled={this.isFacetDisabled(facet)}
        tabIndex={facet.tabIndex}
        focused={this.state.focus}
        children={children}
        hasChildren={hasChildren}
        handleSelectToggleMouse={this.handleSelectToggleMouse}
        handleExpandableToggle={this.handleExpandableToggle}
        styleFacet={styleFacet(this.isFacetDisabled(facet))}
        styleFocus={styleRovingFocus}
        styleCheckboxFocus={styleRovingFocusCheckbox}
        styleChildren={styleExpandableContent(this.props.marginNest)}
        setSize={facetInMap.setSize}
        posInSet={facetInMap.posInSet}
      />
    )
    return facetChildren
  }

  handleKeyUp = e => {
    // do nothing if modifiers are pressed
    if (e.metaKey || e.shiftKey || e.ctrlKey || e.altKey) {
      return
    }

    e.stopPropagation()

    if (e.keyCode === Key.SPACE || e.keyCode === Key.ENTER) {
      e.preventDefault() // prevent scrolling down on space press
      const facet = this.lookupFacet(this.state.rovingIndex)
      const {facetId, category, term, selected, count} = facet
      if (!this.isFacetDisabled(facet)) {
        this.props.handleSelectToggle(
          {id: facetId, category: category, term: term},
          !selected
        )
      }
    }
    if (e.keyCode === Key.HOME) {
      this.moveFocusToStart()
    }
    if (e.keyCode === Key.END) {
      this.moveFocusToEnd()
    }
    if (e.keyCode === Key.UP) {
      e.preventDefault() // prevent scrolling behavior
      this.moveFocusUp()
    }
    if (e.keyCode === Key.DOWN) {
      e.preventDefault() // prevent scrolling behavior
      this.moveFocusDown()
    }
    if (e.keyCode === Key.LEFT) {
      this.triggerLeft()
    }
    if (e.keyCode === Key.RIGHT) {
      this.triggerRight()
    }
  }

  handleKeyDown = e => {
    // prevent the default behavior for tree control keys
    // these are the control keys used by the tree menu
    const treeControlKeys = [
      Key.SPACE,
      Key.ENTER,
      Key.HOME,
      Key.END,
      Key.UP,
      Key.DOWN,
      Key.LEFT,
      Key.RIGHT,
    ]
    if (
      !e.metaKey &&
      !e.shiftKey &&
      !e.ctrlKey &&
      !e.altKey &&
      treeControlKeys.includes(e.keyCode)
    ) {
      e.preventDefault()
    }
  }

  handleFocus = e => {
    if (e.target.id === this.state.treeId) {
      document.getElementById(this.state.rovingIndex).focus()
    }
  }

  render() {
    const facetHierarchy = _.map(this.state.hierarchy, facet => {
      return this.createFacetComponent(facet)
    })

    return (
      <div
        tabIndex="-1"
        onFocus={this.handleFocus}
        role="tree"
        id={this.state.treeId}
        aria-labelledby={this.props.headerId}
        aria-multiselectable="true"
        aria-describedby="facetFilterInstructions"
        onKeyUp={this.handleKeyUp} // onKeyDown isnt an actual keypress
        onKeyDown={this.handleKeyDown}
      >
        {facetHierarchy}
      </div>
    )
  }
}
