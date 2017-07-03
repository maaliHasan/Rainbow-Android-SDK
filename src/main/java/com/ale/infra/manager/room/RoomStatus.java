package com.ale.infra.manager.room;

/**
 * Created by georges on 16/09/2016.
 */
public enum RoomStatus {

    DELETED("deleted"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    PENDING("pending"),
    INVITED("invited"),
    UNSUBSCRIBED("unsubscribed"),
    UNKNOWN("unknown");

    protected String status;

    RoomStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return status;
    }

    public static RoomStatus fromString(String text) {
        if (text != null) {
            for (RoomStatus status : RoomStatus.values()) {
                if (text.equalsIgnoreCase(status.status)) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }

    public boolean isDeleted() {
        return this.status.equals(DELETED.toString());
    }

    public boolean isAccepted() {
        return this.status.equals(ACCEPTED.toString());
    }

    public boolean isRejected() {
        return this.status.equals(REJECTED.toString());
    }

    public boolean isPending() {
        return this.status.equals(PENDING.toString());
    }

    public boolean isInvited() {
        return this.status.equals(INVITED.toString());
    }

    public boolean isUnsubscribed() {
        return this.status.equals(UNSUBSCRIBED.toString());
    }

}
