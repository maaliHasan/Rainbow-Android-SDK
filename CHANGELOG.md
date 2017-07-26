# SDK android.

## [1.27.0] - 2017-07-26

### Changed
* Push deactivated for others applications which are not Rainbow.
* Add web rtc class in Rainbow SDK : allow to develop applications auround wrbRTC : makeCall, TakeCall, addVideo, dropVideo, rejectCall, hangupCall, switchCamera, ITelephonyListener (use to manage incoming calls)...
* add getContactFromId api to have the IContact object from an id


## [1.27.0] - 2017-06-??

### Changed
* Ticket 27581 Rework permissions + tutorial "1. Getting_started.md"
* Ticket 27835 First step of injection API key and secret to SDK
* New methods in RainbowSdk: initialize() [for Rainbow], getHost()
* New optional parameter for method signin:host from Connection
* Remove module Utils



## [1.26.0] - 2017-06-22

### Changed

* Ticket 27579 API contacts (addRainbowContactToRoster, removeContactFromRoster)
* Ticket 27580 API Profile (updateLastName, updateFirstName, updateNickName, updateJobTitle, updatePhoto) + tests
* Ticket 27898 : CPAAS / SDK Android / Manage Invitation API with methods : inviteUserNotRegisterToRainbow, acceptInvitation, declineInvitation, getSentInvitations, getReceivedInvitations, getPendingReceivedInvitations, getPendingSentInvitations, getInvitationById
* New module MyProfile with methods: getConnectedUser, setPresenceTo
* Remove module Presence
* Ticket 27899 : CPAAS / SDK Android / Bubble API first step (get bubble, open iot and send and receive messages)
* Remove method getConnectedUser from Contacts
* New method in Contacts: searchByJid
* New methods in IRainbowContact: getJobTitle, getMainEmailAddress, getFirstEmailAddress
* New status (SUCCESS, ERROR, TIMEOUT) parameter when get messages or more messages from a conversation