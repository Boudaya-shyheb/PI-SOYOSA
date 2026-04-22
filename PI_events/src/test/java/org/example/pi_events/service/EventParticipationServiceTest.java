package org.example.pi_events.service;

import org.example.pi_events.DTO.EventParticipationDTO;
import org.example.pi_events.entity.Event;
import org.example.pi_events.entity.EventParticipation;
import org.example.pi_events.repository.EventParticipationRepository;
import org.example.pi_events.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventParticipationServiceTest {

    @Mock
    private EventParticipationRepository participationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventParticipationService service;

    private Event event;
    private EventParticipationDTO dto;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(9L);
        event.setMaxParticipants(3);

        dto = new EventParticipationDTO();
        dto.setUniversity("INSAT");
        dto.setMotivation("Join");
    }

    @Test
    void participate_success() {
        when(eventRepository.findById(9L)).thenReturn(Optional.of(event));
        when(participationRepository.findByEventIdAndEmail(9L, "client@test.com")).thenReturn(Optional.empty());
        when(participationRepository.save(any(EventParticipation.class))).thenAnswer(invocation -> {
            EventParticipation p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        EventParticipationDTO result = service.participate(9L, dto, "client@test.com");

        assertEquals("PENDING", result.getStatus());
        assertEquals("client@test.com", result.getEmail());
    }



    @Test
    void getParticipationsByEvent_success() {
        EventParticipation approved = participation(1L, "APPROVED", LocalDate.of(2026, 1, 1));
        EventParticipation pending = participation(2L, "PENDING", LocalDate.of(2026, 1, 2));

        when(eventRepository.findById(9L)).thenReturn(Optional.of(event));
        when(participationRepository.findByEventId(9L)).thenReturn(List.of(approved, pending));

        List<EventParticipationDTO> result = service.getParticipationsByEvent(9L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getParticipationsByEvent_fail() {
        when(eventRepository.findById(9L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getParticipationsByEvent(9L));

        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void getPendingRequestsByEvent_success() {
        EventParticipation pending = participation(3L, "PENDING", LocalDate.of(2026, 1, 3));
        when(eventRepository.findById(9L)).thenReturn(Optional.of(event));
        when(participationRepository.findByEventIdAndStatusOrderByParticipationDateDesc(9L, "PENDING"))
                .thenReturn(List.of(pending));

        List<EventParticipationDTO> result = service.getPendingRequestsByEvent(9L);

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void getPendingRequestsByEvent_fail() {
        when(eventRepository.findById(9L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getPendingRequestsByEvent(9L));

        assertEquals("Event not found", ex.getMessage());
    }

    @Test
    void getMyParticipationRequests_success() {
        EventParticipation approved = participation(4L, "APPROVED", LocalDate.of(2026, 1, 4));
        when(participationRepository.findByEmailOrderByParticipationDateDesc("client@test.com")).thenReturn(List.of(approved));

        List<EventParticipationDTO> result = service.getMyParticipationRequests("client@test.com");

        assertEquals(1, result.size());
        assertEquals("client@test.com", result.get(0).getEmail());
    }



    @Test
    void approveParticipationRequest_success() {
        EventParticipation pending = participation(5L, "PENDING", LocalDate.now());
        when(participationRepository.findById(5L)).thenReturn(Optional.of(pending));
        when(participationRepository.findByEventId(9L)).thenReturn(List.of());
        when(participationRepository.save(any(EventParticipation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventParticipationDTO result = service.approveParticipationRequest(5L);

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void approveParticipationRequest_fail() {
        EventParticipation approved = participation(6L, "APPROVED", LocalDate.now());
        when(participationRepository.findById(6L)).thenReturn(Optional.of(approved));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.approveParticipationRequest(6L));

        assertEquals("Only pending requests can be approved", ex.getMessage());
    }

    @Test
    void rejectParticipationRequest_success() {
        EventParticipation pending = participation(7L, "PENDING", LocalDate.now());
        when(participationRepository.findById(7L)).thenReturn(Optional.of(pending));
        when(participationRepository.save(any(EventParticipation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventParticipationDTO result = service.rejectParticipationRequest(7L);

        assertEquals("REJECTED", result.getStatus());
    }

    @Test
    void rejectParticipationRequest_fail() {
        EventParticipation approved = participation(8L, "APPROVED", LocalDate.now());
        when(participationRepository.findById(8L)).thenReturn(Optional.of(approved));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.rejectParticipationRequest(8L));

        assertEquals("Only pending requests can be rejected", ex.getMessage());
    }

    @Test
    void cancelParticipation_success() {
        doNothing().when(participationRepository).deleteById(9L);

        service.cancelParticipation(9L);

        verify(participationRepository).deleteById(9L);
    }

    @Test
    void cancelParticipation_fail() {
        doThrow(new RuntimeException("delete failed")).when(participationRepository).deleteById(9L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.cancelParticipation(9L));

        assertEquals("delete failed", ex.getMessage());
    }

    @Test
    void getParticipationById_success() {
        EventParticipation participation = participation(10L, "APPROVED", LocalDate.now());
        when(participationRepository.findById(10L)).thenReturn(Optional.of(participation));

        EventParticipationDTO result = service.getParticipationById(10L);

        assertEquals(10L, result.getId());
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void getParticipationById_fail() {
        when(participationRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getParticipationById(10L));

        assertEquals("Participation not found", ex.getMessage());
        verify(participationRepository, never()).save(any(EventParticipation.class));
    }

    private EventParticipation participation(Long id, String status, LocalDate date) {
        EventParticipation p = new EventParticipation();
        p.setId(id);
        p.setStatus(status);
        p.setParticipationDate(date);
        p.setFullName("John Doe");
        p.setEmail("client@test.com");
        p.setPhone("123");
        p.setUniversity("INSAT");
        p.setLevel("L3");
        p.setMotivation("Join");
        p.setEvent(event);
        return p;
    }
}
