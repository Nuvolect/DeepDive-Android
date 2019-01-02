



~~~
V/DeepDive:class com.nuvolect.deepdive.webserver.SSLUtil: Certificate length: 5582
V/DeepDive:WEB_SERVICE: Server started: https://10.0.1.6:8022
W/ActivityThread: serviceCreate lasts for 13041 ms, CreateServiceData = CreateServiceData{token=android.os.BinderProxy@33d368a className=com.nuvolect.deepdive.webserver.WebService packageName=com.nuvolect.deepdive.debug intent=null}
W/ActivityThread: serviceStart starts in 13909 ms, ServiceArgsData = ServiceArgsData{token=android.os.BinderProxy@33d368a startId=1 args=Intent { cmp=com.nuvolect.deepdive.debug/com.nuvolect.deepdive.webserver.WebService }}
I/Choreographer: Skipped 783 frames!  The application may be doing too much work on its main thread.
I/Adreno: QUALCOMM build                   : 12b5963, I6fd668c4d3
    Build Date                       : 10/04/18
    OpenGL ES Shader Compiler Version: EV031.25.03.01
    Local Branch                     :
    Remote Branch                    : refs/tags/AU_LINUX_ANDROID_LA.UM.7.2.R1.09.00.00.442.049
    Remote Branch                    : NONE
    Reconstruct Branch               : NOTHING
    Build Config                     : S L 6.0.7 AArch32
D/vndksupport: Loading /vendor/lib/hw/gralloc.sdm660.so from current namespace instead of sphal namespace.
D/AndroidRuntime: Shutting down VM
E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.nuvolect.deepdive.debug, PID: 19097
    android.app.RemoteServiceException: Bad notification for startForeground: java.lang.RuntimeException: invalid channel for service notification: Notification(channel=null pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x40 color=0x00000000 vis=PRIVATE)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1905)
        at android.os.Handler.dispatchMessage(Handler.java:106)
        at android.os.Looper.loop(Looper.java:193)
        at android.app.ActivityThread.main(ActivityThread.java:6912)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:493)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:860)
I/Process: Sending signal. PID: 19097 SIG: 9
Application terminated.
~~~