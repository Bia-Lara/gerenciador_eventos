package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.OrganizerRepository;
import br.ifsp.demo.dto.CreateEventRequest;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    private OrganizerRepository organizerRepository;

    public EventService() {
    }

    public EventService(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    public Event createEvent(CreateEventRequest request){

        return new Event(
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                request.organizerId()
        );
    }
}
