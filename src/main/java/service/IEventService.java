package service;

import domain.Event;

public interface IEventService {
    public void publishEvent(Event event);
}
