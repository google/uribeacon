## UriBeacon

### Project Status Update

We launched the UriBeacon project in 2014 to explore how to use BLE technology to share URLs.  Since then, we’ve been thrilled about the community that has formed around UriBeacon, and with the contributors and partners who have worked with us to produce UriBeacon utilities, apps, prototypes, and production-ready beacons.

We’ve also found that in addition to broadcasting URLs, there is much more that can be accomplished with an open beacon format.

To that end, **UriBeacon is evolving to become part of the [Eddystone open beacon format](http://github.com/google/eddystone)**!

Like UriBeacon, Eddystone supports broadcasting URLs in BTLE advertisement packets via its [Eddystone-URL](https://github.com/google/eddystone/tree/master/eddystone-url/) frame type.  But unlike UriBeacon, Eddystone offers first class support for a variety of payload types.  Eddystone provides much better support for unique IDs via its [Eddystone-UID](https://github.com/google/eddystone/tree/master/eddystone-uid/) frame, provides telemetry information via its [Eddystone-TLM](https://github.com/google/eddystone/tree/master/eddystone-tlm/) frame, and it opens the door for more future innovation.  Read the full [Protocol Specification](https://github.com/google/eddystone/tree/master/protocol-specification.md).

There are some protocol changes to this new format, but this move should not impact much for the [Physical Web](http://physical-web.org) project, which will support Eddystone-URL beacons.

We look forward to continuing to work with you in the Eddystone-URL project!

—The UriBeacon (and now Eddystone-URL) team

### Old Repo Contents

If you’re looking for the old UriBeacon specification and code, you can find it tagged [here](https://github.com/google/uribeacon/tree/uribeacon-final).

