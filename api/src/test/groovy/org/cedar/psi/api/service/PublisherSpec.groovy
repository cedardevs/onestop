package org.cedar.psi.api.service

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import spock.lang.Specification


class PublisherSpec extends Specification {

  def mockProducer = Mock(Producer)
  def publisher = new Publisher(mockProducer).with {
    it.GRANULE_TOPIC = 'test_granule'
    it.COLLECTION_TOPIC = 'test_collection'
    return it
  }

  def 'publishes valid granules'() {
    def data = '{"trackingId": "ABC", "path": "/test/file.txt"}'

    when:
    publisher.publishGranule(data)

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && it.key() == 'ABC' && it.value() == data})
  }

  def 'does not publish granules if no tracking id present'() {
    def data = '{"path": "/test/file.txt"}'

    when:
    publisher.publishGranule(data)

    then:
    0 * mockProducer.send(_)
  }

  def 'publishes collections with a given id'() {
    def id = 'ABC'
    def data = '<text>xml woooo....</text>'

    when:
    publisher.publishCollection(data, id)

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && it.key() == 'ABC' && it.value().contains(data)})
  }

  // TODO - not sure that we actually want to do this in the future
  def 'makes up an id for collections without a given id'() {
    def data = '<text>xml woooo....</text>'

    when:
    publisher.publishCollection(data)

    then:
    1 * mockProducer.send({it instanceof ProducerRecord && it.key() != null && it.value().contains(data)})
  }

}
