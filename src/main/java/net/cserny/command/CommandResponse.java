package net.cserny.command;

public record CommandResponse(Status status) {

    public static final CommandResponse EMPTY = new CommandResponse(Status.SUCCESS);

    public enum Status {
        SUCCESS,
        NOT_FOUND,
        FAILED;
    }
}
