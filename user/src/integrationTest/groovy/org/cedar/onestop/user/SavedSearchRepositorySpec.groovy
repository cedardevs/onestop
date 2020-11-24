package org.cedar.onestop.user

import org.cedar.onestop.user.domain.OnestopUser
import org.cedar.onestop.user.domain.SavedSearch
import org.cedar.onestop.user.repository.OnestopUserRepository
import org.cedar.onestop.user.repository.SavedSearchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
class SavedSearchRepositorySpec extends Specification {

  @Autowired
  SavedSearchRepository saveSearchRepository

  @Autowired
  OnestopUserRepository onestopUserRepo

  OnestopUser savedUser
  SavedSearch saveSearch

  def setup(){
    onestopUserRepo.deleteAll()
    OnestopUser onestopUser = new OnestopUser("mock_user")
    savedUser = onestopUserRepo.save(onestopUser)
    saveSearch = new SavedSearch(savedUser, "1", "entryName1","{\"test\":\"test\"}", "value 1")
  }

  def "should Store Each SaveSearch"() {
    given:
    SavedSearch saveSearch1 = new SavedSearch(savedUser, "2", "entryName2", "{\"test\":\"test\"}","value 1")
    saveSearchRepository.save(saveSearch)
    saveSearchRepository.save(saveSearch1)

    when:
    long count = saveSearchRepository.count()

    then:
    count == 2
  }

  def "Should store with a unique identifier"() {
    given:
    def id = saveSearchRepository.save(saveSearch)

    when:
    def getById = saveSearchRepository.getOne(id.getId())

    then:
    getById == id
  }

  def "Should get by user Identifier"() {
    given:
    def search = saveSearchRepository.save(saveSearch)

    when:
    List<SavedSearch> getByUserId = saveSearchRepository.findByUserId(savedUser.id, null).getContent()

    then:
    getByUserId[0]?.id == search.id
    getByUserId[0]?.getUser()?.getId() == savedUser.getId()
  }

  def "should have multiple entries for a userId"() {
    given:
    SavedSearch saveSearch1 = new SavedSearch(savedUser, "2", "entryName2", "{\"test\":\"test\"}", "value 2")
    SavedSearch saveSearch2 = new SavedSearch(savedUser, "3", "entryName3", "{\"test\":\"test\"}","value 3")

    saveSearchRepository.save(saveSearch1)
    saveSearchRepository.save(saveSearch2)

    when:
    List<SavedSearch> getByUserId = saveSearchRepository.findByUserId(savedUser.id, null).getContent()

    then:
    getByUserId.size() == 2
    getByUserId[0].value == "value 2"
    getByUserId[1].value == "value 3"
    getByUserId[0].getUser() == getByUserId[1].getUser()
  }

}

