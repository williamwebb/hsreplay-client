hsreplay-client | [![Release](https://jitpack.io/v/io.williamwebb/hsreplay-client.svg)](https://jitpack.io/#io.williamwebb/hsreplay-client) [![CircleCI](https://circleci.com/gh/williamwebb/hsreplay-client.svg?style=svg)](https://circleci.com/gh/williamwebb/hsreplay-client)
============

hsreplay client

Usage
=====

```
val client = HSReplayClient(
    okHttpClient: OkHttpClient, 
    apiKey: String, 
    test: /* Boolean = false */
)

client.createAuthToken()

client.getAuthToken()

client.claimReplaysRequest()

client.upload(uploadRequest, power_log)
```

Installation
=====
```
// Add the JitPack repo
repositories {
    maven { url 'https://jitpack.io' }
}

// Add dependency
dependencies {
    compile 'io.williamwebb:hsreplay-client:<version>'
}
```
License
-------

    Copyright 2017 William Webb

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.