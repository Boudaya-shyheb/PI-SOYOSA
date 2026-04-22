package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubJoinRequestDto;
import org.example.pi_events.DTO.ClubMemberDto;
import org.example.pi_events.service.ClubMemberService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/club-members")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ClubMemberController {

    private static final String AUTH_DISABLED_DEFAULT_EMAIL = "admin@pi-event.tn";
    private final ClubMemberService memberService;

    @PostMapping
    public ClubMemberDto createMember(@RequestBody ClubMemberDto dto) {
        return memberService.createMember(dto);
    }

    @PostMapping("/join/{clubId}")
    public ClubJoinRequestDto joinClub(
            @PathVariable Long clubId,
            Principal principal,
            @RequestParam(required = false) String email
    ) {
        return memberService.joinClub(clubId, resolveEmail(principal, email));
    }

    @GetMapping("/requests/pending/{clubId}")
    public List<ClubJoinRequestDto> getPendingRequestsByClub(@PathVariable Long clubId) {
        return memberService.getPendingRequestsByClub(clubId);
    }

    @GetMapping("/requests/me")
    public List<ClubJoinRequestDto> getMyJoinRequests(
            Principal principal,
            @RequestParam(required = false) String email
    ) {
        return memberService.getMyJoinRequestStatuses(resolveEmail(principal, email));
    }

    @PutMapping("/requests/{requestId}/approve")
    public ClubJoinRequestDto approveJoinRequest(@PathVariable Long requestId) {
        return memberService.approveJoinRequest(requestId);
    }

    @PutMapping("/requests/{requestId}/reject")
    public ClubJoinRequestDto rejectJoinRequest(@PathVariable Long requestId) {
        return memberService.rejectJoinRequest(requestId);
    }

    @GetMapping
    public List<ClubMemberDto> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public ClubMemberDto getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id);
    }

    @PutMapping("/{id}")
    public ClubMemberDto updateMember(@PathVariable Long id, @RequestBody ClubMemberDto dto) {
        return memberService.updateMember(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }

    private String resolveEmail(Principal principal, String email) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return AUTH_DISABLED_DEFAULT_EMAIL;
    }
}
