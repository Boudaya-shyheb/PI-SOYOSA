package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.ClubJoinRequestDto;
import org.example.pi_events.DTO.ClubMemberDto;
import org.example.pi_events.entity.Club;
import org.example.pi_events.entity.ClubJoinRequest;
import org.example.pi_events.entity.ClubJoinRequestStatus;
import org.example.pi_events.entity.ClubMember;
import org.example.pi_events.entity.ClubMemberRole;
import org.example.pi_events.repository.ClubJoinRequestRepository;
import org.example.pi_events.repository.ClubMemberRepository;
import org.example.pi_events.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMemberService {

    private final ClubMemberRepository memberRepository;
    private final ClubJoinRequestRepository joinRequestRepository;
    private final ClubRepository clubRepository;

    public ClubMemberDto createMember(ClubMemberDto dto) {

        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));

        if (memberRepository.existsByClubIdAndEmail(dto.getClubId(), dto.getEmail())) {
            throw new RuntimeException("This user is already a member of this club");
        }

        if (club.getMaxMembers() != null) {
            long currentMembers = memberRepository.countByClubId(dto.getClubId());
            if (currentMembers >= club.getMaxMembers()) {
                throw new RuntimeException("Club is full");
            }
        }

        ClubMember member = new ClubMember();
        member.setFirstName(resolveFirstName(dto.getFirstName(), dto.getEmail()));
        member.setLastName(resolveLastName(dto.getLastName(), dto.getEmail()));
        member.setEmail(dto.getEmail());
        member.setPhoneNumber(dto.getPhoneNumber());
        member.setAge(dto.getAge());
        member.setEducationLevel(dto.getEducationLevel());
        member.setRole(dto.getRole());

        // 🔥 RELATION
        member.setClub(club);

        return toDTO(memberRepository.save(member));
    }

    public List<ClubMemberDto> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ClubMemberDto getMemberById(Long id) {
        ClubMember member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return toDTO(member);
    }

    public ClubMemberDto updateMember(Long id, ClubMemberDto dto) {

        ClubMember member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Club club = clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));

        boolean emailChanged = !member.getEmail().equalsIgnoreCase(dto.getEmail());
        boolean clubChanged = !member.getClub().getId().equals(dto.getClubId());
        if ((emailChanged || clubChanged) && memberRepository.existsByClubIdAndEmail(dto.getClubId(), dto.getEmail())) {
            throw new RuntimeException("This user is already a member of this club");
        }

        member.setFirstName(resolveFirstName(dto.getFirstName(), dto.getEmail()));
        member.setLastName(resolveLastName(dto.getLastName(), dto.getEmail()));
        member.setEmail(dto.getEmail());
        member.setPhoneNumber(dto.getPhoneNumber());
        member.setAge(dto.getAge());
        member.setEducationLevel(dto.getEducationLevel());
        member.setRole(dto.getRole());

        // 🔥 UPDATE RELATION
        member.setClub(club);

        return toDTO(memberRepository.save(member));
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public ClubJoinRequestDto joinClub(Long clubId, String userEmail) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        String normalizedEmail = normalizeEmail(userEmail);
        if (memberRepository.existsByClubIdAndEmail(clubId, normalizedEmail)) {
            throw new RuntimeException("You are already a member of this club");
        }

        ClubJoinRequest request = joinRequestRepository.findByClubIdAndEmail(clubId, normalizedEmail)
                .orElseGet(ClubJoinRequest::new);

        if (request.getId() != null) {
            if (request.getStatus() == ClubJoinRequestStatus.PENDING) {
                throw new RuntimeException("Your request is already pending admin approval");
            }
            if (request.getStatus() == ClubJoinRequestStatus.APPROVED) {
                throw new RuntimeException("Your request has already been approved");
            }
            request.setStatus(ClubJoinRequestStatus.PENDING);
            request.setRequestedAt(LocalDateTime.now());
            request.setReviewedAt(null);
        } else {
            request.setEmail(normalizedEmail);
            request.setFirstName(resolveFirstName(null, normalizedEmail));
            request.setLastName(resolveLastName(null, normalizedEmail));
            request.setStatus(ClubJoinRequestStatus.PENDING);
            request.setRequestedAt(LocalDateTime.now());
            request.setClub(club);
        }

        return toJoinRequestDTO(joinRequestRepository.save(request));
    }

    public List<ClubJoinRequestDto> getPendingRequestsByClub(Long clubId) {
        clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        return joinRequestRepository.findByClubIdAndStatusOrderByRequestedAtDesc(clubId, ClubJoinRequestStatus.PENDING)
                .stream()
                .map(this::toJoinRequestDTO)
                .toList();
    }

    public List<ClubJoinRequestDto> getMyJoinRequestStatuses(String userEmail) {
        return joinRequestRepository.findByEmailOrderByRequestedAtDesc(normalizeEmail(userEmail))
                .stream()
                .map(this::toJoinRequestDTO)
                .toList();
    }

    public ClubJoinRequestDto approveJoinRequest(Long requestId) {
        ClubJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found"));

        if (request.getStatus() != ClubJoinRequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        Long clubId = request.getClub().getId();
        if (memberRepository.existsByClubIdAndEmail(clubId, request.getEmail())) {
            throw new RuntimeException("This user is already a member of this club");
        }

        Club club = request.getClub();
        if (club.getMaxMembers() != null) {
            long currentMembers = memberRepository.countByClubId(clubId);
            if (currentMembers >= club.getMaxMembers()) {
                throw new RuntimeException("Club is full");
            }
        }

        ClubMember member = new ClubMember();
        member.setFirstName(resolveFirstName(request.getFirstName(), request.getEmail()));
        member.setLastName(resolveLastName(request.getLastName(), request.getEmail()));
        member.setEmail(request.getEmail());
        member.setPhoneNumber(null);
        member.setAge(null);
        member.setEducationLevel(null);
        member.setRole(ClubMemberRole.MEMBER);
        member.setClub(club);
        memberRepository.save(member);

        request.setStatus(ClubJoinRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        return toJoinRequestDTO(joinRequestRepository.save(request));
    }

    public ClubJoinRequestDto rejectJoinRequest(Long requestId) {
        ClubJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found"));

        if (request.getStatus() == ClubJoinRequestStatus.APPROVED) {
            throw new RuntimeException("Approved request cannot be rejected");
        }

        request.setStatus(ClubJoinRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        return toJoinRequestDTO(joinRequestRepository.save(request));
    }

    private String resolveFirstName(String firstName, String email) {
        if (firstName != null && !firstName.isBlank()) {
            return firstName.trim();
        }
        String normalizedEmail = normalizeEmail(email);
        int atIndex = normalizedEmail.indexOf('@');
        if (atIndex > 0) {
            return normalizedEmail.substring(0, atIndex);
        }
        return "User";
    }

    private String resolveLastName(String lastName, String email) {
        if (lastName != null && !lastName.isBlank()) {
            return lastName.trim();
        }
        String normalizedEmail = normalizeEmail(email);
        int atIndex = normalizedEmail.indexOf('@');
        if (atIndex > 0) {
            return normalizedEmail.substring(0, atIndex);
        }
        return normalizedEmail;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "guest@local";
        }
        return email.trim().toLowerCase();
    }

    private ClubMemberDto toDTO(ClubMember member) {
        return new ClubMemberDto(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getAge(),
                member.getEducationLevel(),
                member.getRole(),
                member.getClub().getId() // 🔥 important
        );
    }

    private ClubJoinRequestDto toJoinRequestDTO(ClubJoinRequest request) {
        return new ClubJoinRequestDto(
                request.getId(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getReviewedAt(),
                request.getClub().getId()
        );
    }
}
