package com.test.mongodb_ssl_local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class MongoDbSslLocalTestApplicationTests {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    void contextLoads() {
    }

    @BeforeEach
    public void setUp() {
        // 더미 데이터 삽입
        User user1 = new User();
        user1.setName("John");
        user1.setAge(30);

        User user2 = new User();
        user2.setName("Alice");
        user2.setAge(25);

        mongoTemplate.save(user1);
        mongoTemplate.save(user2);
    }
    @Test
    public void testInsert() {
        User user = new User();
        user.setName("John Doe");
        user.setAge(30);

        // 데이터 삽입
        User insertedUser = mongoTemplate.insert(user, "users");

        // 삽입된 데이터 확인
        System.out.println("Inserted User ID: " + insertedUser.getId());
    }

    @Test
    public void testFindAll() {
        // 조회 테스트
        List<User> users = mongoTemplate.findAll(User.class);
        for (User user : users) {
            System.out.println(user);
        }
    }
    @Test
    public void testDeleteUserByName() {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("John"));
        mongoTemplate.remove(query, User.class);

        List<User> users = mongoTemplate.findAll(User.class);
        for (User user : users) {
            System.out.println(user);
        }
    }

}
