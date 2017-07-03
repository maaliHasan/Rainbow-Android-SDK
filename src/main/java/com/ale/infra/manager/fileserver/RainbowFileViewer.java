package com.ale.infra.manager.fileserver;

/**
 * Created by georges on 16/02/2017.
 */

public class RainbowFileViewer {

    private String id;
    private ViewerType type;


    public RainbowFileViewer() {
    }

    public RainbowFileViewer(String viewerId, ViewerType type) {
        this.id = viewerId;
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = ViewerType.fromString(type);
    }

    public ViewerType getType() {
        return type;
    }

    public boolean isUser() {
        return type != null && type.equals(ViewerType.USER);
    }

    public boolean isRoom() {
        return type != null && type.equals(ViewerType.ROOM);
    }

    public enum ViewerType {
        USER("user"),
        ROOM("room");

        protected String type;

        ViewerType(String type)
        {
            this.type = type;
        }

        @Override
        public String toString()
        {
            return type;
        }

        public static ViewerType fromString(String type) {
            if (type != null) {
                for (ViewerType currentType : ViewerType.values()) {
                    if (type.equalsIgnoreCase(currentType.type)) {
                        return currentType;
                    }
                }
            }
            return null;
        }

        public String getType()
        {
            return this.type;
        }
    }
}
