package eu.opertusmundi.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class ServiceResponse<R> {

    @Getter
    private R result;

    private final List<Message> messages = new ArrayList<Message>();

    protected ServiceResponse() {
    }

    protected ServiceResponse(R result) {
        this.result = result;
    }

    protected ServiceResponse(Message message) {
        this.messages.add(message);
    }

    protected ServiceResponse(List<Message> messages) {
        this.messages.addAll(messages);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

    public static <R> ServiceResponse<R> success() {
        return new ServiceResponse<>();
    }

    public static <R> ServiceResponse<R> result(R r) {
        return new ServiceResponse<>(r);
    }

    public static <R> ServiceResponse<R> error(MessageCode code, String description, Message.EnumLevel level) {
        return ServiceResponse.<R>error(new Message(code, description, level));
    }

    public static <R> ServiceResponse<R> error(MessageCode code, String description) {
        return error(code, description, Message.EnumLevel.ERROR);
    }

    public static <R> ServiceResponse<R> error(Message message) {
        return new ServiceResponse<R>(message);
    }

    public static <R> ServiceResponse<R> error(List<Message> messages) {
        return new ServiceResponse<R>(messages);
    }

}
