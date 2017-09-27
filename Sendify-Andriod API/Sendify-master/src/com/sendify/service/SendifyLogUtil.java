package com.sendify.service;

public class SendifyLogUtil {

	 @SuppressWarnings("unchecked")
    public static String makeLogTag(Class cls) {
        return ("Sendify_" + cls.getSimpleName());
    }
}
