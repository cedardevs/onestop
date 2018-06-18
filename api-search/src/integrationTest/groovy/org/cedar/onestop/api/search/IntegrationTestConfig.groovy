package org.cedar.onestop.api.search

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.Wait

@Profile("integration")
@TestConfiguration
class IntegrationTestConfig {

  @Value('${elasticsearch.version}')
  String esVersion

  @Bean(name = 'esContainer', initMethod = 'start', destroyMethod = 'stop')
  GenericContainer esContainer() {
    String dockerImageName = "docker.elastic.co/elasticsearch/elasticsearch:${esVersion}"
    GenericContainer esContainer = new GenericContainer(dockerImageName)
        .withExposedPorts(9200)
        .waitingFor(Wait.forHttp('/'))
        .withEnv("xpack.security.enabled", "false")
        .withEnv("transport.host", "127.0.0.1")
        .withEnv("http.host", "0.0.0.0")
        .withEnv("ES_JAVA_OPTS", "-Xms1g -Xmx1g")
    return esContainer
  }

  @Bean(destroyMethod = 'close')
  @DependsOn('esContainer')
  RestClient restClient() {
    def esContainer = esContainer()
    def restClient = RestClient.builder(
        new HttpHost(esContainer.getContainerIpAddress(), esContainer.getMappedPort(9200))
    ).build()
    return restClient
  }

  @Bean
  Map testData() {
    return [
        'DEM': [
            'C1': [
                id: 'e7a36e60-1bcb-47b1-ac0d-3c2a2a743f9b',
                granules: [],
                flattenedGranules: []
            ],
            'C2': [
                id: 'e5820283-3686-44d0-8edd-28a086eb500e',
                granules: [],
                flattenedGranules: []
            ],
            'C3': [
                id: '1415b3db-c602-4dbb-a502-4091fe9df1cf',
                granules: [],
                flattenedGranules: []
            ],
        ],
        'GHRSST': [
            'C1': [
                id: '920d8155-f764-4777-b7e5-14442b7275b8',
                granules: [],
                flattenedGranules: []
            ],
            'C2': [
                id: '882511bc-e99e-4597-b634-47a59ddf9fda',
                granules: [],
                flattenedGranules: []
            ],
            'C3': [
                id: '42ea683d-e4e7-434c-8823-abff32e00f34',
                granules: [],
                flattenedGranules: []
            ],
        ],
        'COOPS': [
            'C1': [
                id: 'fcf83ec9-964b-45b9-befe-378ea6ce52cb',
                granules: [
                    'G1': [id: '783089c4-3484-4f70-ac8d-d4818d0cd0dd'],
                    'G2': [id: 'a207b48f-29fc-4d79-a676-1f265cd9971f'],
                ],
                flattenedGranules: [
                    'FG1': [id: '783089c4-3484-4f70-ac8d-d4818d0cd0dd'],
                    'FG2': [id: 'a207b48f-29fc-4d79-a676-1f265cd9971f']
                ]
            ]
        ]
    ]
  }
}
