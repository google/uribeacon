var uriBeacon = require('uri-beacon');

// txPowerLevel can be set in options hash
// should be between -100 and 20 dBm
uriBeacon.advertise("http://www.eff.org", { txPowerLevel: -31 });
