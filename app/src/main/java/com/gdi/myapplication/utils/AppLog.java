package com.gdi.myapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class AppLog {

    public static void i(String message) {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        StackTraceElement currentStack = stacks[1];
        String[] classNameArray = currentStack.getClassName().split("\\.");
        String className = classNameArray[classNameArray.length - 1];
        String logMessage = "[" + className + "." + currentStack.getMethodName() + "]" + message;
    }
}
