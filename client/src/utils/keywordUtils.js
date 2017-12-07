import _ from 'lodash'

const buildHierarchyMap = (category, terms, selectedFacets) => {
  // const createChildrenHierarchy = (map, hierarchy, term, value) => {
  //   // console.log('creating hierarchy' , map, hierarchy, term, value)
  //   // This function traverses down the hierarchy specified in the given map, creating empty 'children' objects ONLY if
  //   //   they don't already exist (otherwise just changes the reference). Since hierarchical strings are received tokenized
  //   //   and in alphabetical order (e.g.: 'Atmosphere', 'Atmosphere > Aerosols', 'Biosphere', etc.) this traversal down
  //   //   the nested object won't error out from one level to the next.
  //   const lastTerm = hierarchy.pop()
  //   if (!_.isEmpty(hierarchy)) {
  //     let i
  //     for (i = 0; i < hierarchy.length; i++) {
  //       map = map[hierarchy[i]].children = map[hierarchy[i]].children || {}
  //     }
  //   }
  //
  //   map = map[lastTerm] = value
  //   return map
  // }
  //
  // let categoryMap = {}

  // console.log('creating layer' , category, terms)
  /*
  Object.keys(terms).map(term => {
    const idParts = _.concat(_.words(category), _.words(term))
    let hierarchy = term.split('>').map(e => e.trim()) // Handling unfortunate instances of strings like "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave"
    console.log(hierarchy)
    // console.log('terms loop', term, idParts.join('-'))
    const value = {
      count: terms[term].count,
      children: {},
      category: category,
      term: term,
      id: idParts.join('-'),
      // open: false, // always default to everything collapsed
      // tabIndex: '-1',
      // value.relations.parent = parentId
      // value.relations.children = this.parseMap(
      //   value.children,
      //   level + 1,
      //   parentOpen && value.open,
      //   value.id
      // )
      // value.visible = level === 1 || !!parentOpen
    }
    createChildrenHierarchy(categoryMap, hierarchy, term, value)
  })*/

  let lastParent = {
    0: {id: null, children:[]}
  }
  let parent

  _.each(terms, (data, term)=>{
    const idParts = _.concat(_.words(category), _.words(term))
    let hierarchy = term.split('>').map(e => e.trim()) // Handling unfortunate instances of strings like "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave"
    let t = hierarchy.pop()
    // if(hierarchy.length === 0){
    // let facet = {
    //   count: data.count,
    //   category: category,
    //   term: term,
    //   name: t,
    //   id: idParts.join('-'),
    //   children: [],
    //   parentId: lastParent[hierarchy.length].id,
    // }
    // let facet = {
    //   ...buildFacet(category, term, data.count)}
    // let baseFacet = buildFacet(category, term, data.count)
    // console.log('base', baseFacet)
    const facet = {
      ...buildFacet(category, term, data.count, selectedFacets),
      name: titleCaseKeyword(t),
      children: [],
      parentId: lastParent[hierarchy.length].id,
    }

    lastParent[hierarchy.length+1] = facet
    // console.log(term, hierarchy.length, lastParent[hierarchy.length])
    lastParent[hierarchy.length].children.push(facet)

    // }
    /*if(hierarchy.length === 1) {
      let t = hierarchy.pop()
      console.log(category, t, hierarchy, data.count)
      if(hierarchy.length === 0){
        parent = { // TODO change to array.push instead
          count: data.count,
          category: category,
          term: term,
          name: t,
          id: null, //TODO
          children: [],
        }
        categoryMap[t] = parent
      }
    }

    while(hierarchy.length > 0) {
      let t = hierarchy.pop()
      console.log('layers?', parent, category, name, term)
      if(hierarchy.length === 0){
        let newParent = { // TODO change to array.push instead
          count: data.count,
          category: category,
          term: term,
          name: t,
          id: null, //TODO
          children: [],
        }
        parent.children.push(newParent)
        parent = newParent
      }

    }
*/
  })
  console.log('DONE', lastParent[0].children)
  // return categoryMap
  lastParent[0].children
}

const buildFacet = (category, term, count, selectedFacets) => {
  const idParts = _.concat(_.words(category), _.words(term))
  return {
    count: count,
    category: category,
    term: term,
    id: idParts.join('-'),
    // selected: isSelected(selectedFacets, category, term),
  }
}

const categoryName = category => {
  if (category === 'science') {
    return 'Data Theme'
  }
  return _.startCase(_.toLower(category.split(/(?=[A-Z])/).join(' ')))
}

const facets = (category, terms, selectedFacets) => {
  if (category === 'science') {
    return buildHierarchyMap(category, terms, selectedFacets)
  }
  return _.map(terms, (data, term)=>{ return buildFacet(category, term, data.count, selectedFacets) })
}

export const buildKeywordHierarchyMap = (facetMap, selectedFacets) => {
  // console.log('init the facet map', facetMap)
  const hierarchyMap = {}
  return _.map(facetMap, (terms, category) => {
    if (!_.isEmpty(terms)) {
      // Don't load categories that have no results
      // let heading
      // let categoryMap = {}
      //
      // if (category === 'science') {
      //   heading = 'Data Theme'
      //   categoryMap = buildHierarchyMap(category, terms)
      // }
      // else {
      //   heading = _.startCase(_.toLower(category.split(/(?=[A-Z])/).join(' ')))
      //   Object.keys(terms).map(term => {
      //     const idParts = _.concat(_.words(category), _.words(term))
      //     categoryMap[term] = {
      //       count: terms[term].count,
      //       children: {},
      //       category: category,
      //       term: term,
      //       id: idParts.join('-'),
      //     }
      //   })
      // }

      // hierarchyMap[heading] = categoryMap
      console.log('loop', category, terms)
      return {
        name: categoryName(category),
        keywordFacets: facets(category, terms, selectedFacets),
      }
    }
  })

  // return hierarchyMap
}

const isSelected = (selectedFacets, category, term) => {
  const selectedTerms = selectedFacets[category]
  return selectedTerms ? selectedTerms.includes(term) : false
}
  // parseMap = (map, level, parentOpen, parentId) => {
  //   if (_.isEqual({}, map)) {
  //     // cannot parse empty map
  //     return []
  //   }
  //
  //   _.each(map, (value, key) => {
  //     value.relations = {}
  //     value.open = false // always default to everything collapsed
  //     value.tabIndex = '-1'
  //   })
  //
  //   if (level === 1) {
  //     // id first layer to set the initial tab focus
  //     const value = _.map(map, (value, key) => value)[0]
  //     value.tabIndex = '0'
  //     this.setState(prevState => {
  //       return {
  //         ...prevState,
  //         rovingIndex: value.id,
  //       }
  //     })
  //   }
  //
  //   _.each(map, (value, key) => {
  //     this.setState(prevState => {
  //       // update state that lets us quickly traverse the nodes in up/down order
  //       let allFacetsInOrder = Object.assign([], prevState.allFacetsInOrder)
  //       allFacetsInOrder.push(value.id)
  //
  //       // update state that lets us set focus to another node or update visibility, since it is a property that combines the state of several nodes
  //       let facetLookup = Object.assign({}, prevState.facetLookup)
  //       facetLookup[value.id] = value
  //
  //       return {
  //         ...prevState,
  //         allFacetsInOrder: allFacetsInOrder,
  //         facetLookup: facetLookup,
  //       }
  //     })
  //
  //     value.relations.parent = parentId
  //     value.relations.children = this.parseMap(
  //       value.children,
  //       level + 1,
  //       parentOpen && value.open,
  //       value.id
  //     )
  //     value.visible = level === 1 || !!parentOpen
  //   })
  //
  //   return _.map(map, (value, key) => value.id) // return siblings
  // }
// pulls out the last term in a GCMD-style keyword and attempts to maintain intended acronyms
export const titleCaseKeyword = term => {
  if (!term) {
    return null
  }
  const trimmed = term.split('>').pop().trim() // TODO no more need to split, combine with categoryName?
  return trimmed === trimmed.toUpperCase()
    ? _.startCase(trimmed.toLowerCase())
    : trimmed
}
