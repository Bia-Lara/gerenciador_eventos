package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.CreateEventRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EventService {

    public Event createEvent(CreateEventRequest request){

        return new Event(
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                request.organizerId()
        );
    }
}
