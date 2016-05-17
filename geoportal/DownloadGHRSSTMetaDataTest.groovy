package ngdc

import groovy.json.JsonSlurper
import spock.lang.Specification

public class DownloadGHRSSTMetaDataTest extends Specification {

	void "test download some GHRSST collection metadata xml"() {
		setup:
		def apiString = 'http://data.nodc.noaa.gov/geoportal/rest/find/document?searchText=title:GHRSST%20NOT%20title:Documentation&start=1&max=100&f=json'
		URL apiUrl = new URL(apiString)
		def json = new JsonSlurper().parse(apiUrl)
		when: ""
		then: ""
		println("json:${json}")
		println("links:")
		int i = 0
		json.records.each() { record ->
			record.links.each() { link ->
				if (link.type.contains("metadata")) {
//					if (i == 0 ) {
						println("${link.href}")
						String linkString = link.href
						def name = linkString.substring(linkString.indexOf("=") + 1)
						name = name.replace('%7B', '')
						name = name.replace('%7D', '')
						println("name:${name}")
						def text = new URL(link.href).getText()
//						println("text:${text}")
						new File("data/${name}.xml") << new URL (link.href).getText()
//						i++
//					}
				}
			}
		}
	}
}
