What is this?
-------------

This is piece of code implements a protocol for a [Dining Cryptographers](http://en.wikipedia.org/wiki/Dining_cryptographers_problem).

It offers a server that sums up and participants.


How to use it in my code?
-------------------------

Import this as project to eclipse and include it into your code that shall use
DC communication.

You will need to create a _Participant_ object and call the
_establishNewConnection()_ method.  Memorize the _Participant_ object and the
returning _Connection_ object.

Send new messages to the DC-Network by invoking the _feedWorkCycleManager()_ of
the _Connection_ object. 

How to run the DC-Server?
-------------------------

Export _dc--_ as _dc--.jar_. Then call:
	`java -jar dc--.jar <options>`

If you run the jar without any options, you'll be presented a help, describing
all available options.

