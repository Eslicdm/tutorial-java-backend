package com.eslirodrigues.tutorialjavabackend.api.controller;

import com.eslirodrigues.tutorialjavabackend.api.database.model.Member;
import com.eslirodrigues.tutorialjavabackend.api.database.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class MockitoMemberControllerTest {

    private MemberRepository memberRepository;
    private MemberController controller;
    private Principal principal;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        controller = new MemberController(memberRepository);
        principal = () -> "esli";
    }

    @Test
    void shouldReturnAMemberById() {
        Member member = new Member(1L, "esli", 30, "esli", List.of(), null);
        when(memberRepository.findByIdAndOwner(1L, "esli")).thenReturn(member);

        var response = controller.findMemberById(1L, principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(member);
    }

    @Test
    void shouldNotReturnAMemberWithUnknownId() {
        when(memberRepository.findByIdAndOwner(1000L, "esli")).thenReturn(null);

        var response = controller.findMemberById(1000L, principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldReturnAllMembersForOwner() {
        List<Member> members = List.of(
                new Member(1L, "esli", 30, "esli", List.of("Lucas", "Ana"), null),
                new Member(2L, "alice", 25, "esli", List.of(), null)
        );
        Page<Member> page = new PageImpl<>(members);
        when(memberRepository.findByOwner(eq("esli"), any(Pageable.class))).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("age"));
        var response = controller.findAllMembersByOwner(pageable, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(members);
    }

    @Test
    void shouldCreateANewMember() {
        Member newMember = new Member(null, "carl", 20, "esli", List.of("ChildA", "ChildB"), null);
        Member newMemberSaved = new Member(33L, "carl", 20, "esli", List.of("ChildA", "ChildB"), null);
        UriComponentsBuilder ucb = UriComponentsBuilder.fromPath("");
        when(memberRepository.save(any())).thenReturn(newMemberSaved);

        var response = controller.createMember(newMember, ucb, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getHeaders().getLocation()).toString())
                .isEqualTo("/members/33");

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member newMemberCaptured = captor.getValue();
        assertThat(newMemberCaptured.getName()).isEqualTo("carl");
        assertThat(newMemberCaptured.getOwner()).isEqualTo("esli");
        assertThat(newMemberCaptured.getAge()).isEqualTo(20);
        assertThat(newMemberCaptured.getSons()).containsExactly("ChildA", "ChildB");
    }

    @Test
    void shouldUpdateAnExistingMember() {
        Member update = new Member(null, "esli", 99, "esli", List.of("UpdatedSon"), null);
        Member existing = new Member(1L, "esli", 10, "esli", List.of("SomeSon"), null);
        when(memberRepository.findByIdAndOwner(1L, "esli")).thenReturn(existing);

        var response = controller.updateMember(1L, update, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(memberRepository).save(argThat(updated ->
                updated.getId().equals(1L)
                        && updated.getAge().equals(99)
                        && updated.getSons().contains("UpdatedSon")
                        && updated.getName().equals("esli")
        ));
    }

    @Test
    void shouldNotUpdateAMemberThatDoesNotExist() {
        Member update = new Member(null, "unknow", 60, "esli", List.of("UpdatedSon"), null);
        when(memberRepository.findByIdAndOwner(99999L, "esli")).thenReturn(null);

        var response = controller.updateMember(99999L, update, principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteAnExistingMember() {
        when(memberRepository.existsByIdAndOwner(1L, "esli")).thenReturn(true);

        var response = controller.deleteMember(1L, principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(memberRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteAMemberThatDoesNotExist() {
        when(memberRepository.existsByIdAndOwner(99999L, "esli")).thenReturn(false);

        var response = controller.deleteMember(99999L, principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(memberRepository, never()).deleteById(anyLong());
    }
}