# This is my README

Upcoming Features
=================
* Modify Events button => button which is to allow the user pull up a list of all silenced events for the next 24 hours and unsilence or modify them
* Popup info window for hush a calendar page => shows an dialog about the event when you touch it

TODO
====
* Change hushcal events database so that start and end times are longs instead of datetime
  + this will allow for easier integration between between java values and SQL values
* work out some sort of unique ID system for events
  + we could possibly run into problems with two events having the same name
* work on syncing with online google calendar
  + this will allow the app to work on older devices and sync with ALL calendars, it currently only syncs with calendars that are stored on the phone
* do some testing to make sure that silencing an event is unscheduled when the status is changed
* make sure data is persistent when updated in app store
