import _ from 'lodash'
import Immutable from 'seamless-immutable'

const hierarchy = (category, facets) => {
  const reducer = (map, facet) => {
    const termHierarchy = facet.termHierarchy
    const facetMap = {
      id: facet.id,
      children: [],
      parent: map[termHierarchy.length].id,
    }
    map[termHierarchy.length + 1] = facetMap
    map[termHierarchy.length].children.push(facetMap)
    return map
  }
  let initState = {}
  initState[0] = {id: null, children: []}
  return facets.reduce(reducer, initState)[0].children
}

const id = (category, term) => {
  const idParts = _.concat(_.words(category), _.words(term))
  return idParts.join('-')
}

const buildTermHierarchy = (term, isHierarchy) => {
  if (isHierarchy) {
    return term.split(' > ')
  }
  return [ term.split(' > ').pop() ]
}

const buildFacet = (category, term, count, selected, isHierarchy) => {
  const termHierarchy = buildTermHierarchy(term, isHierarchy)
  return Immutable({
    count: count,
    category: category,
    term: term,
    id: id(category, term),
    selected: selected,
    termHierarchy: termHierarchy,
    keyword: termHierarchy.pop(),
  })
}

const categoryName = category => {
  if (category === 'science') {
    return 'Data Theme'
  }
  return _.startCase(_.toLower(category.split(/(?=[A-Z])/).join(' ')))
}

const determineIfHierarchy = category => {
  return (
    category === 'science' ||
    category === 'services' ||
    category === 'dataFormats'
  )
}

const facets = (category, terms, selectedFacets) => {
  const selectedTerms = selectedFacets[category]

  return _.map(terms, (data, term) => {
    let selected = selectedTerms ? selectedTerms.includes(term) : false
    return buildFacet(
      category,
      term,
      data.count,
      selected,
      determineIfHierarchy(category)
    )
  })
}

export const buildFilterHierarchyMap = (facetMap, selectedFacets) => {
  return _.map(facetMap, (terms, category) => {
    if (!_.isEmpty(terms) && category !== 'locations') {
      // Don't load categories that have no results & don't load Locations category
      const keywordFacets = facets(category, terms, selectedFacets)
      return {
        name: categoryName(category),
        id: categoryName(category).replace(' ', '-', 'g'),
        keywordFacets: keywordFacets,
        hierarchy: Immutable(hierarchy(category, keywordFacets)),
      }
    }
  }).filter(facetCategory => {
    return facetCategory
  })
}
