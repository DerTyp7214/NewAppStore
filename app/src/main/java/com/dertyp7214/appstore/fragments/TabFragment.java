/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.dertyp7214.appstore.Config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

public class TabFragment extends Fragment {

    public String getName(Context context){
        return "";
    }

    public boolean serverOnline(){
        try {
            URL url = new URL(Config.API_URL);
            SocketAddress sockaddr = new InetSocketAddress(InetAddress.getByName(url.getHost()), 80);
            Socket sock = new Socket();
            int timeoutMs = 2000;
            sock.connect(sockaddr, timeoutMs);
            return true;
        } catch(IOException ignored) {
        }
        return false;
    }

    public boolean haveConnection(){
        try {
            URL url = new URL("http://www.google.de");
            SocketAddress sockaddr = new InetSocketAddress(InetAddress.getByName(url.getHost()), 80);
            Socket sock = new Socket();
            int timeoutMs = 2000;
            sock.connect(sockaddr, timeoutMs);
            return true;
        } catch(IOException ignored) {
        }
        return false;
    }

}
