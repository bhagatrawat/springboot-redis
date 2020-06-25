package com.bhagat.redis.repository;

import com.bhagat.redis.RedisAppMain;
import com.bhagat.redis.model.Address;
import com.bhagat.redis.model.Gender;
import com.bhagat.redis.model.Person;
import com.bhagat.redis.test.util.EmbeddedRedisServer;
import com.bhagat.redis.test.util.RequiresRedisServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisAppMain.class)
@TestPropertySource(properties = {"create.enabled=false"})
public class PersonRepositoryTest {

    /**
     * Create Redis server instance
     */
    public static @ClassRule
    RuleChain rules = RuleChain
            .outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions())
            .around(RequiresRedisServer.onLocalhost());

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Autowired
    RedisOperations<Object, Object> operations;
    @Autowired
    PersonRepository repository;

    Person eddard = new Person("eddard", "stark", Gender.MALE);
    Person robb = new Person("robb", "stark", Gender.MALE);
    Person sansa = new Person("sansa", "stark", Gender.FEMALE);
    Person arya = new Person("arya", "stark", Gender.FEMALE);
    Person bran = new Person("bran", "stark", Gender.MALE);
    Person rickon = new Person("rickon", "stark", Gender.MALE);
    Person jon = new Person("jon", "snow", Gender.MALE);

    @Before
    @After
    public void setUp() {
        operations.execute((RedisConnection connection) -> {
            connection.flushDb();
            return "OK";
        });
    }

    /**
     * Save a single entity and verify that a key for the given keyspace/prefix exists. <br />
     * Print out the hash structure within Redis.
     */
    @Test
    public void saveSingleEntity() {
        repository.save(eddard);
        assertThat(operations
                .execute((RedisConnection connection) -> connection.exists(("persons:" + eddard.getId()).getBytes(CHARSET))))
                .isTrue();
    }

    @Test
    public void findBySingleProperty() {
        flushTestUsers();
        List<Person> starks = repository.findByLastname(eddard.getLastname());
        assertThat(starks).contains(eddard, robb, sansa, arya, bran, rickon).doesNotContain(jon);
    }

    @Test
    public void findByMultipleProperties() {
        flushTestUsers();
        List<Person> aryaStark = repository.findByFirstnameAndLastname(arya.getFirstname(), arya.getLastname());
        assertThat(aryaStark).containsOnly(arya);
    }

    @Test
    public void findByMultiplePropertiesUsingOr() {
        flushTestUsers();
        List<Person> aryaAndJon = repository.findByFirstnameOrLastname(arya.getFirstname(), jon.getLastname());
        assertThat(aryaAndJon).containsOnly(arya, jon);
    }

    @Test
    public void findByQueryByExample() {
        flushTestUsers();
        Example<Person> example = Example.of(new Person(null, "stark", null));
        Iterable<Person> starks = repository.findAll(example);
        assertThat(starks).contains(arya, eddard).doesNotContain(jon);
    }

    @Test
    public void findByReturningPage() {
        flushTestUsers();
        Page<Person> page1 = repository.findPersonByLastname(eddard.getLastname(), PageRequest.of(0, 5));
        assertThat(page1.getNumberOfElements()).isEqualTo(5);
        assertThat(page1.getTotalElements()).isEqualTo(6);
        Page<Person> page2 = repository.findPersonByLastname(eddard.getLastname(), PageRequest.of(1, 5));
        assertThat(page2.getNumberOfElements()).isEqualTo(1);
        assertThat(page2.getTotalElements()).isEqualTo(6);
    }

    @Test
    public void findByEmbeddedProperty() {
        Address winterfell = new Address();
        winterfell.setCountry("the north");
        winterfell.setCity("winterfell");
        eddard.setAddress(winterfell);
        flushTestUsers();
        List<Person> eddardStark = repository.findByAddress_City(winterfell.getCity());
        assertThat(eddardStark).containsOnly(eddard);
    }

    /**
     * Store references to other entities without embedding all data. <br />
     * Print out the hash structure within Redis.
     */
    @Test
    public void useReferencesToStoreDataToOtherObjects() {
        flushTestUsers();
        eddard.setChildren(Arrays.asList(jon, robb, sansa, arya, bran, rickon));
        repository.save(eddard);
        assertThat(repository.findById(eddard.getId())).hasValueSatisfying(it -> {
            assertThat(it.getChildren()).contains(jon, robb, sansa, arya, bran, rickon);
        });
        repository.deleteAll(Arrays.asList(robb, jon));
        assertThat(repository.findById(eddard.getId())).hasValueSatisfying(it -> {
            assertThat(it.getChildren()).contains(sansa, arya, bran, rickon);
            assertThat(it.getChildren()).doesNotContain(robb, jon);
        });
    }

    private void flushTestUsers() {
        repository.saveAll(Arrays.asList(eddard, robb, sansa, arya, bran, rickon, jon));
    }
}
