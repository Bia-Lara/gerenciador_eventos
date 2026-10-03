package br.ifsp.demo.services;

import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EventQueryServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;

    private EventQueryService sut;

    @BeforeEach
    void setUp() {
        sut = new EventQueryService(userRepository, eventRepository);
    }
}
