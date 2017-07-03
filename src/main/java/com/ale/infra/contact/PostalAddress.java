package com.ale.infra.contact;

/**
 * Created by grobert on 18/05/16.
 */
public class PostalAddress {

    public PostalAddress(AddressType type, String value) {
        this.type = type;
        this.value = value;
    }

    public enum AddressType {
        WORK("work"), HOME("home"), OTHER("other"), CUSTOM("custom"), UNKNOWN("unknown");
        private String value;

        AddressType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }


    private String value;
    private AddressType type;

    public AddressType getType() {
        return type;
    }

    public void setType(AddressType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostalAddress)) return false;
        PostalAddress that = (PostalAddress) o;
        return (this.type==that.getType()) &&
                this.value.equalsIgnoreCase(that.getValue());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }
}
