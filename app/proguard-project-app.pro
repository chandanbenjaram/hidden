# The simpliest strategy is to not run proguard against your project's own code.
# This doesn't provide the benefits of optimization & obfuscation against your 
# project, but will still strip the libraries. The advantage is that your app will
# work without any subsequent effort. If you choose this strategy, the proguard
# configuration for the project is simply the line below.

-keep class co.samepinch.android.app.** { *; }

# The more involved strategy is to specifically provide rules to keep portions of your
# app's codebase unmodified while allowing proguard to optimize the rest. 

# Additionally you will need to keep specific classes. A common use case is keeping all
# of the models that are JSON parsed using something like Jackson.

#-keep class com.yourpackage.app.model.User { *; }

-keep public class android.widget.ShareActionProvider {
  public *;
}

-dontwarn com.facebook.**
-dontwarn com.facebook.fresco**
-dontwarn it.sephiroth.**
-dontwarn com.squareup.**
-dontwarn org.springframework.**
-dontwarn org.apache.http.**
-dontwarn com.parse.**
-dontwarn com.aviary.android.**
#-dontwarn org.apache.lang.**
#-dontwarn org.apache.commons.**
#-dontwarn com.nhaarman.**
#-dontwarn se.emilsjolander.**