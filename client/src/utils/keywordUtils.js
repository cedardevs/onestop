import _ from 'lodash'

const buildHierarchyMap = (category, terms, isSelected) => {
  let lastParent = {
    0: {id: null, children:[]} // initial state of what is essentially a custom map-reduce // TODO use actual map reduce?
  }

  _.each(terms, (data, term)=>{
    const idParts = _.concat(_.words(category), _.words(term))
    let hierarchy = term.split('>').map(e => e.trim()) // Handling unfortunate instances of strings like "Spectral/Engineering >\t\t\t\t\t\t\tmicrowave"
    let keyword = hierarchy.pop()
    const facet = {
      ...buildFacet(category, term, data.count, isSelected(category, term)),
      keyword: titleCaseKeyword(keyword),
      children: [],
      parentId: lastParent[hierarchy.length].id,
    }

    lastParent[hierarchy.length+1] = facet
    lastParent[hierarchy.length].children.push(facet)

  })

  return lastParent[0].children
}

const buildFacet = (category, term, count, selected) => {
  const idParts = _.concat(_.words(category), _.words(term))
  return {
    count: count,
    category: category,
    term: term,
    id: idParts.join('-'),
    keyword: term,
    selected: selected,
    // selected: isSelected(selectedFacets, category, term),
  }
}

const categoryName = category => {
  if (category === 'science') {
    return 'Data Theme'
  }
  return _.startCase(_.toLower(category.split(/(?=[A-Z])/).join(' ')))
}

const facets = (category, terms, isSelected) => {
  if (category === 'science') {
    return buildHierarchyMap(category, terms, isSelected)
  }
  return _.map(terms, (data, term)=>{ return buildFacet(category, term, data.count, isSelected(category, term)) })
}

export const buildKeywordHierarchyMap = (facetMap, selectedFacets) => {
  const hierarchyMap = {}
  return _.map(facetMap, (terms, category) => {
    if (!_.isEmpty(terms)) {
      // Don't load categories that have no results
      return {
        name: categoryName(category),
        id: categoryName(category).replace(' ', '-', 'g'),
        keywordFacets: facets(category, terms, isSelected(selectedFacets)),
      }
    }
  }).filter((facetCategory)=> {return facetCategory}) // remove undefined?

}

const isSelected = (selectedFacets) => { // TODO still not working right
  return (category, term) => {
    const selectedTerms = selectedFacets[category]
    return selectedTerms ? selectedTerms.includes(term) : false
  }
}

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
