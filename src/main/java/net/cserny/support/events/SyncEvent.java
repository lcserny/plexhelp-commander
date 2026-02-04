package net.cserny.support.events;

import lombok.Data;

@Data
public abstract class SyncEvent<E> {

    protected E result;
}
