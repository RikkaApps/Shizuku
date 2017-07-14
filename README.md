# Why is this application born?
In Android system, many APIs require apps to be system-privileged or have a framework signature to call them.
Some apps that require root permission may only need to call those APIs. Due to limitations mentioned above, alternative methods that do not require calling those APIs directly will be used, which are usually much slower than calling APIs directly.
For a better user experience, we need a faster and better solution. Thanks very much for [Brevent](https://github.com/brevent/Brevent) that provides ideas and some code. So Shizuku Server was born. It also makes non-root usages become possible (via adb).

# What is Shizuku Server?
Shizuku Server is a process started by root or adb, therefore the process can invoke some privileged APIs that ordinary applications do not have permission to call, and ordinary applications can be invoked by interacting with the process. There is just a little gap between the speed of calling Shizuku Server and calling API directly.

# What is a Shizuku Manager?
Shizuku Manager is used to start Shizuku Server and manage the apps who can use this service. In order to prevent being abused by malware, ordinary applications need to request user's consent by Shizuku Manager before they can use those privileged APIs. If you want to install the app that uses Shizuku Server under multi-users, you need to install Shizuku Manager in the corresponding user.

# How to start the Shizuku Server?
If you have your device rooted already, you can simply start the server in Shizuku Manager.
If you have not got your device rooted, you can follow the instructions in Shizuku Manager and start the server via adb. It is not really difficult in using adb and you can learn about it easily on the Internet by yourself.

Here is a video about how to setup Shizuku Server via adb:
https://youtu.be/Nk24nhn0Jcs

# To developer:
Except for the authorization section, it is very easy to use the Shizuku Server, as simple as calling API directly.
Due to the enormous number of APIs, so far only a few APIs are available.
