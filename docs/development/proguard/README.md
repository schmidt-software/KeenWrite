From https://github.com/greenrobot/EventBus#r8-proguard

	-keepattributes *Annotation*
	-keepclassmembers class * {
			@org.greenrobot.eventbus.Subscribe <methods>;
	}
	-keep enum org.greenrobot.eventbus.ThreadMode { *; }
	 
