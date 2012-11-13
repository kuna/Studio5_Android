package com.swm.studio5.tool;

import java.io.IOException;

import android.hardware.Camera;

public class ExceptionTool {
    public static void reconnect(Camera c) {
        try {
                c.reconnect();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
