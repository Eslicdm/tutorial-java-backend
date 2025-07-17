package com.eslirodrigues.tutorialjavabackend.api.controller;

import com.eslirodrigues.tutorialjavabackend.api.database.model.Member;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/data.sql")
@ActiveProfiles("test")
class MemberControllerTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Value("${BASIC_DB_USERNAME}")
    private String ownerUsername;

    @Value("${BASIC_DB_PASSWORD}")
    private String ownerPassword;

    @Value("${BASIC_DB_GUESTNAME}")
    private String guestUsername;

    @Value("${BASIC_DB_GUESTPW}")
    private String guestPassword;


    @Test
    void shouldReturnAMemberById() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .getForEntity("/members/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("esli");
    }

    @Test
    void shouldNotReturnAMemberWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .getForEntity("/members/1000", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    void shouldReturnAllMembersForOwner() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .getForEntity("/members?page=0&size=10", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext json = JsonPath.parse(response.getBody());
        List<String> names = json.read("$[*].name");
        assertThat(names).containsExactlyInAnyOrder("esli", "alice");

        List<String> owners = json.read("[*].owner");
        assertThat(owners).allMatch(own -> own.equals("esli"));
    }


    @Test
    @DirtiesContext
    void shouldCreateANewMember() {
        Member newMember = new Member(null, "carl", 20, "esli", List.of("ChildA", "ChildB"), null);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .postForEntity("/members", newMember, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewUser = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .getForEntity(locationOfNewUser, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Integer age = documentContext.read("$.age");
        String name = documentContext.read("$.name");
        assertThat(id).isNotNull();
        assertThat(age).isEqualTo(20);
        assertThat(name).isEqualTo("carl");
    }

    @Test
    void shouldNotReturnAMemberWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/members/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("esli", "BAD-PASSWORD")
                .getForEntity("/members/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingMember() {
        Member updateMember = new Member(null, "esli", 99, "esli", List.of("UpdatedSon"), null);
        HttpEntity<Member> request = new HttpEntity<>(updateMember);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .exchange("/members/1", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .getForEntity("/members/1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Integer age = documentContext.read("$.age");
        assertThat(age).isEqualTo(99);
    }

    @Test
    void shouldNotUpdateAMemberThatDoesNotExist() {
        Member unknownUser = new Member(null, "unknow", 60, "esli", List.of("UpdatedSon"), null);
        HttpEntity<Member> request = new HttpEntity<>(unknownUser);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .exchange("/members/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAnExistingMember() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .exchange("/members/1", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .getForEntity("/members/1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteAMemberThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(ownerUsername, ownerPassword)
                .exchange("/members/99999", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAllowAccessForNonOwners() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth(guestUsername, guestPassword)
                .getForEntity("/members/1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth(guestUsername, guestPassword)
                .exchange("/members/1", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}