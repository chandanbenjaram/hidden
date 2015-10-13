# hidden
https://guides.codepath.com/android/Handling-Scrolls-with-CoordinatorLayout

https://github.com/bumptech/glide/wiki

http://jakewharton.github.io/butterknife/


https://github.com/facebook/fresco/issues/259

http://databasefaq.com/index.php/answer/79292/android-performance-view-gif-animated-gif-best-practice-of-showing-gif-in-android

https://guides.codepath.com/android/Must-Have-Libraries (cool)

###COMMANDS
adb shell ps|grep "co.samepinch.android.app"|awk '{print $2}'|xargs adb shell kill 


ARM Translator - https://goo.gl/lC6GWt


Gapps(v2-4) - http://goo.gl/0aPPhx


Gapps for v5(Lollipop) - http://goo.gl/0zOqQq


https://github.com/mihaip/dex-method-counts


###RELEASE
--where release apk
app/build/outputs/apk

align and verify


Android/sdk//build-tools/23.0.0/zipalign -f -v 4 app-release.apk app-release_out.apk


Android/sdk//build-tools/23.0.0/zipalign -c -v 4 app-release_out.apk
