package com.eslirodrigues.tutorialjavabackend.api.controller;

import com.eslirodrigues.tutorialjavabackend.api.database.model.Member;
import com.eslirodrigues.tutorialjavabackend.api.database.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    private Member findMember(Long id, Principal principal) {
        return memberRepository.findByIdAndOwner(id, principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> findMemberById(@PathVariable Long id, Principal principal) {
        Member member = findMember(id, principal);
        return member != null ? ResponseEntity.ok(member) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Member>> findAllMembersByOwner(Pageable pageable, Principal principal) {
        Page<Member> page = memberRepository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "age"))
                )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    public ResponseEntity<Void> createMember(
            @RequestBody Member newMemberRequest,
            UriComponentsBuilder ucb,
            Principal principal
    ) {
        Member memberWithName = new Member(
                null,
                newMemberRequest.getName(),
                newMemberRequest.getAge(),
                principal.getName(),
                newMemberRequest.getSons(),
                newMemberRequest.getDeletedDate()
        );
        Member savedMember = memberRepository.save(memberWithName);

        URI location = ucb.path("/members/{id}").buildAndExpand(savedMember.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMember(
            @PathVariable Long id,
            @RequestBody Member memberUpdate,
            Principal principal
    ) {
        Member member = findMember(id, principal);
        if (member != null) {
            Member updatedMember = new Member(
                    id,
                    memberUpdate.getName(),
                    memberUpdate.getAge(),
                    principal.getName(),
                    memberUpdate.getSons(),
                    memberUpdate.getDeletedDate()
            );
            memberRepository.save(updatedMember);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id, Principal principal) {
        if (memberRepository.existsByIdAndOwner(id, principal.getName())) {
            memberRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
