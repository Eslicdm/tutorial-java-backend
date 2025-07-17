package com.eslirodrigues.tutorialjavabackend.api.database;

import com.eslirodrigues.tutorialjavabackend.api.database.model.Member;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class MemberJsonTest {

    @Autowired
    private JacksonTester<Member> json;

    @Autowired
    private JacksonTester<Member[]> jsonList;

    private Member[] members;

    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @BeforeEach
    void setUp() {
        members = Arrays.array(
                new Member(1L,"esli", 30, "esli", List.of("Lucas","Ana"), null),
                new Member(2L,"alice", 25, "esli", List.of(), LocalDateTime.parse("2024-12-31 23:59:59", DB_FORMATTER)),
                new Member(3L,"bob", 40, "bill", List.of("Eva"), null)
        );
    }

    @Test
    void userSerializationTest() throws IOException {
        Member member = members[0];

        assertThat(json.write(member)).hasJsonPathStringValue("@.name");
        assertThat(json.write(member)).extractingJsonPathStringValue("@.name").isEqualTo("esli");
        assertThat(json.write(member)).hasJsonPathNumberValue("@.age");
        assertThat(json.write(member)).extractingJsonPathNumberValue("@.age").isEqualTo(30);
        assertThat(json.write(member)).hasJsonPathArrayValue("@.sons");
    }

    @Test
    void userDeserializationTest() throws IOException {
        String expected = """
            {
                "id": 7,
                "name": "esli",
                "age": 30,
                "owner": "esli",
                "sons": ["Lucas", "Ana"],
                "deletedDate": null
            }
            """;

        Member parsed = json.parseObject(expected);
        assertThat(parsed.getId()).isEqualTo(7L);
        assertThat(parsed.getName()).isEqualTo("esli");
        assertThat(parsed.getAge()).isEqualTo(30);
        assertThat(parsed.getSons()).containsExactly("Lucas", "Ana");
        assertThat(parsed.getDeletedDate()).isNull();
    }

    @Test
    void userListSerializationTest() throws IOException {
        assertThat(jsonList.write(members)).hasJsonPathArrayValue("$");
        assertThat(jsonList.write(members)).extractingJsonPathArrayValue("$").hasSize(3);
    }

    @Test
    void userListDeserializationTest() throws IOException {
        String expected = """
            [
              {"id":1,"name":"esli","age":30,"owner":"esli","sons":["Lucas","Ana"],"deletedDate":null},
              {"id":2,"name":"alice","age":25,"owner":"esli","sons":[],"deletedDate":"2024-12-31 23:59:59"},
              {"id":3,"name":"bob","age":40,"owner":"bill","sons":["Eva"],"deletedDate":null}
            ]
            """;
        assertThat(jsonList.parse(expected)).isEqualTo(members);
    }
}
