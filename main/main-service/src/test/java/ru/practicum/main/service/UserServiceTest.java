package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.main.dto.NewUserRequest;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ParticipationRequestRepository requestRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void create_savesUserAndReturnsDto() {
        NewUserRequest request = NewUserRequest.builder().name("Ivan").email("ivan@test.com").build();
        User saved = User.builder().id(1L).name("Ivan").email("ivan@test.com").build();
        when(userRepository.save(any())).thenReturn(saved);

        assertEquals(1L, userService.create(request).getId());
    }

    @Test
    void getUsers_withoutIds_returnsPagedUsers() {
        User user = User.builder().id(1L).name("Ivan").email("ivan@test.com").build();
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        assertEquals(1, userService.getUsers(null, 0, 10).size());
    }

    @Test
    void delete_whenUserNotFound_throwsNotFound() {
        when(userRepository.existsById(5L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.delete(5L));
    }

    @Test
    void delete_whenUserHasEvents_throwsConflict() {
        when(userRepository.existsById(5L)).thenReturn(true);
        when(eventRepository.existsByInitiatorId(5L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.delete(5L));
    }

    @Test
    void getUserOrThrow_whenMissing_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserOrThrow(1L));
    }
}
