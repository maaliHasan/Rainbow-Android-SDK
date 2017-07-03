package com.ale.infra.contact;

/**
 * Created by georges on 20/02/2017.
 */

public class Profile {
    private String subscriptionId;
    private String offerId;
    private String offerName;
    private String profileId;
    private String profileName;
    private String assignationDate;
    private String profileStatus;
    private boolean isDefault;

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public String getOfferName() {
        return offerName;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setAssignationDate(String assignationDate) {
        this.assignationDate = assignationDate;
    }

    public String getAssignationDate() {
        return assignationDate;
    }

    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    public String getProfileStatus() {
        return profileStatus;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
