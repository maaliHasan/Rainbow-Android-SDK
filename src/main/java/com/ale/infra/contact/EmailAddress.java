package com.ale.infra.contact;

/**
 * Created by grobert on 18/05/16.
 */
public class EmailAddress {

    public EmailAddress(EmailType type, String value) {
        this.type = type;
        this.value = value;
    }

    public enum EmailType {
        WORK("work"), HOME("home"), MOBILE("mobile"), OTHER("other"), CUSTOM("custom"), UNKNOWN("unknown");
        private String value;

        EmailType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    private String value;
    private EmailType type;

    public EmailType getType() {
        return type;
    }

    public void setType(EmailType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    // EmailAddress are equals when values are equals (no check on Type)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailAddress)) return false;
        EmailAddress that = (EmailAddress) o;
        return this.value.equalsIgnoreCase(that.getValue());
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
