package com.ale.infra.contact;

/**
 * Created by grobert on 18/05/16.
 */
public class WebSite
{

    private String value;
    private WebSiteType type;


    public WebSite(WebSiteType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    public WebSiteType getType()
    {
        return type;
    }

    public void setType(WebSiteType type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof WebSite))
            return false;
        WebSite that = (WebSite) o;
        return (this.type == that.getType()) && this.value.equalsIgnoreCase(that.getValue());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    public enum WebSiteType
    {
        WORK("work"),
        HOME("home"),
        HOMEPAGE("homepage"),
        BLOG("blog"),
        OTHER("other"),
        CUSTOM("custom"),
        UNKNOWN("unknown");
        private String value;

        WebSiteType(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

    }
}
