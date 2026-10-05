package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;
import br.ifsp.demo.infrastructure.security.user.User;

import java.util.Optional;
import java.util.UUID;

public class CancelEventServiceImpl implements CancelEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public CancelEventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void execute(UUID eventId, UUID userId) {
        Optional<User> user = userRepository.findById(userId);

        if(user.isEmpty()){
            throw new UserNotFoundException(userId);
        }

    }
}
