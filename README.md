android-puppet
==============

The Android portion of Sense-Pill-Phone integration test rig.

# Building

You will need the following prerequisites.

- [Java](http://support.apple.com/kb/DL1572) (on Yosemite).
- [Android Studio](http://developer.android.com/sdk/index.html).
- The [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (for lambda support).
- The correct SDK and build tools. These will be automatically installed by Android Studio and the Square SDK manager gradle plugin.

If you're building the app on a platform other than OS X, you will need to define `JAVA_HOME` in order for the project to find your installation of the JDK 8.

Once the above conditions are satisfied, you should be able to perform a gradle sync and build and run.

# Design

The app contains a snapshot of the same Bluetooth stack used in the shipping `suripu-android` application. The version name of the app indicates the version of `suripu-android` its Bluetooth stack is from.

# Usage

In order to communicate with Puppet, you will need an Android phone with either [USB](http://stackoverflow.com/questions/16707137/how-to-find-and-turn-on-usb-debugging-mode-on-nexus-4) or WiFi debugging turned on, and a computer with [ADB](https://developer.android.com/sdk/index.html#Other) installed.

All input to Puppet takes the form of system-wide intent broadcasts through ADB, and all output comes in the form of JSON hashes sent to `logcat`.

## Issuing commands

Commands are issued through the application manager via `adb shell`. Commands take the following form:

	adb shell am broadcast \
		-p "is.hello.puppet" \
		-a "<action name>"
		[--es <parameter name> "<parameter value>"]

Puppet only supports running one command at a time, reflecting the real world limitations of `suripu-android`. New commands should not be issued until an `end_command` event is observed over logcat.

Before issuing commands, it's a good idea to ensure that the Puppet application is running. You can do this with the following ADB command:

```bash
adb shell am start -n "is.hello.puppet/is.hello.puppet.ui.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```

### Available Commands

`is.hello.puppet.ACTION_DISCOVER`

Performs a Bluetooth Low Energy scan on the phone to select a Sense for testing. This command must be issued before all other commands.

**Input:**
May include an optional `EXTRA_SENSE_ID` to narrow the scan to a specific
device. If omitted, the device with the strongest relative RSSI will be selected.

**Output:**
The advertised name of the selected Sense.

**Preconditions:**
None.

---

`sense_id` (param)

A factory assigned Sense identifier.

---

`is.hello.puppet.ACTION_CONNECT`

Attempts to connect to the selected Sense, placing the peripheral's
LEDs into trippy mode if successful.

**Input:**
Nothing.

**Output:**
The advertised name of the selected Sense.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

---

`is.hello.puppet.ACTION_DISCONNECT`

Disconnects from the selected Sense. Does nothing if the selected Sense
is not connected, or there is no selected Sense.

**Input:**
Nothing.

**Output:**
The advertised name of the selected Sense, or an empty string if no Sense is selected.

**Preconditions:**
None

---

`is.hello.puppet.ACTION_RESET`

Clears the currently selected peripheral. Should not be issued until all
running commands have emitted an `end_command` event.

**Input:
Nothing.

**Output:**
Nothing.

**Preconditions:**
No commands may be running.

---

`is.hello.puppet.ACTION_PRINT_WIFI_NETWORK`

Outputs the WiFi network currently connected to by the selected Sense.

**Input:**
Nothing.

**Output:**
The name of the network Sense is connected to, or `null` if there is none.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

---

`is.hello.puppet.ACTION_SCAN_WIFI`

Performs a WiFi network scan on the selected Sense.

**Input:**
Nothing.

**Output:**
A comma-separated string containing the SSIDs of the networks scanned by Sense.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

---

`is.hello.puppet.ACTION_CONNECT_WIFI`

Attempts to connect to a WiFi network on the selected Sense.

**Input:**
The extras `EXTRA_WIFI_SSID` and `EXTRA_WIFI_SEC_TYPE` must be specified.
`EXTRA_WIFI_SEC_TYPE` must also be specified unless the network is open.

**Output:**
The SSID of the network Sense connected to.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

---

`wifi_ssid` (param)

A WiFi network SSID. Required to issue a connection attempt.

---

`wifi_sec_type` (param)

A WiFi network security type. Value corresponds to the field names of `sec_type`.

- `SL_SCAN_SEC_TYPE_OPEN`
- `SL_SCAN_SEC_TYPE_WEP`
- `SL_SCAN_SEC_TYPE_WPA`
- `SL_SCAN_SEC_TYPE_WPA2`

---

`wifi_password` (param)

A WiFi network password. Required unless the `EXTRA_WIFI_SEC_TYPE`
is `sec_type#SL_SCAN_SEC_TYPE_OPEN`.

---

`is.hello.puppet.ACTION_LINK_ACCOUNT`

Attempts to link Sense to an account using a given access token.

**Input:**
An access token via `EXTRA_ACCESS_TOKEN`.

**Output:**
The passed in access token.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

---

`is.hello.puppet.ACTION_PAIR_PILL`

Initiates the pill pairing process on the selected Sense.

**Input:**
An access token via `EXTRA_ACCESS_TOKEN`.

**Output:**
The passed in access token.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

---

`access_token` (param)

An access token from the Sense API.

---

`is.hello.puppet.ACTION_FACTORY_RESET`

Issues a factory reset on the selected Sense.

**Input:**
None

**Output:**
The name of the selected Sense.

**Preconditions:**
A Sense must be selected by `ACTION_DISCOVER`.

## Output

To receive output from Puppet, you can use the following ADB command:

```bash
adb logcat -s TestOutput
```

Output will take the format of:

	--------- beginning of system
	--------- beginning of main
	D/TestOutput﹕ {"event":"begin_command","action":"is.hello.puppet.ACTION_DISCOVER"}
	I/TestOutput﹕ {"event":"end_command","action":"is.hello.puppet.ACTION_DISCOVER","result":"Sense-20"}
	E/TestOutput﹕ {"event":"precondition_failed","action":"is.hello.puppet.ACTION_CONNECT","message":"No Sense currently discovered."}

The following are the possible log levels, indicated at the start of log lines:

- (D)ebug
- (I)nfo
- (E)rror

Error logs may include a detailed stack trace after the end of the JSON hash.

### JSON Spec

For ease of parsing through regex, the JSON output is guaranteed to never nest.

```apib
+ event: `begin_command` (required, enum) - What just happened.
	+ Members
		+ `begin_command`
		+ `end_command`
		+ `error`
		+ `precondition_failed`
+ action: (required, enum) - The action sent earlier.
	+ Members
		+ `is.hello.puppet.ACTION_DISCOVER`
		+ `is.hello.puppet.ACTION_CONNECT`
		+ `is.hello.puppet.ACTION_PRINT_WIFI_NETWORK`
		+ `is.hello.puppet.ACTION_SCAN_WIFI`
		+ `is.hello.puppet.ACTION_CONNECT_WIFI`
		+ `is.hello.puppet.ACTION_LINK_ACCOUNT`
		+ `is.hello.puppet.ACTION_PAIR_PILL`
		+ `is.hello.puppet.ACTION_FACTORY_RESET`
		+ `is.hello.puppet.ACTION_DISCONNECT`
+ result: (optional, string) - The result of the command. Used with `endend_command`.
+ message: (optional, string) - The message associated with an error. Used with `error` and `precondition_failed`.
```

## Example flow

The following series of commands simulates a typical on-boarding session within `suripu-android`. The implementation of `wait-and-require-no-error` is left as an exercise for the reader.

```bash
adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_DISCOVER" \
	--es sense_id "<Sense device id>"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_CONNECT"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_PRINT_WIFI_NETWORK"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_SCAN_WIFI"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_CONNECT_WIFI" \
	--es wifi_ssid "Hello" \
	--es wifi_sec_type "SL_SCAN_SEC_TYPE_WPA2" \
	--es wifi_password "[redacted]"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_LINK_ACCOUNT" \
	--es access_token "<API access token>"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_PAIR_PILL" \
	--es access_token "<API access token>"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_FACTORY_RESET"

wait-and-require-no-error

adb shell am broadcast \
	-p "is.hello.puppet" \
	-a "is.hello.puppet.ACTION_RESET"

wait-and-require-no-error
```
