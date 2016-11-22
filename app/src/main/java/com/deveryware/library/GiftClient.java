/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import com.deveryware.emitter.R;
import com.deveryware.gift.GiftQuery;
import com.deveryware.gift.GiftResult;
import com.deveryware.gift.data.Cell;
import com.deveryware.gift.data.Cell.Type;
import com.deveryware.gift.data.Command;
import com.deveryware.gift.data.CommandResponse;
import com.deveryware.gift.data.CommandResponse.CommandState;
import com.deveryware.gift.data.GpsLocation;
import com.deveryware.gift.data.GpsLocation.Validity;
import com.deveryware.gift.data.IPInfos;
import com.deveryware.gift.data.Inputs;
import com.deveryware.gift.data.Phone;
import com.deveryware.gift.data.Phone.APCState;
import com.deveryware.gift.data.Phone.BatteryState;
import com.deveryware.gift.data.Phone.Network;
import com.deveryware.gift.data.Query;
import com.deveryware.gift.data.Result;
import com.deveryware.gift.data.Result.Response;
import com.deveryware.gift.data.ServerCommandDesc;
import com.deveryware.gift.data.Wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.os.BatteryManager;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class GiftClient {

    public static final String CMD_ID = "cmdid";

    public static final String CMD_PARAMS = "params";

    public static final String CMD_ACK = "cmdack";

    public static final String CMD_TEXT = "cmdtext";

    public static final String INPUT_TEXT1 = "text1";

    public static final String INPUT_TEXT2 = "text2";

    public static final String APC = "apc";

    static final GiftQuery query = new GiftQuery();

    static final GiftResult result = new GiftResult();

    static final HashMap<String, String> COMMANDS = new HashMap<String, String>();

    static {
        COMMANDS.put("BLOCK", "com.deveryware.emitter.STOP");
        COMMANDS.put("UNBLOCK", "com.deveryware.emitter.START");
        COMMANDS.put("RESTART", "com.deveryware.emitter.RESTART");
        COMMANDS.put("RING", "com.deveryware.emitter.RING");
        COMMANDS.put("ASK", "com.deveryware.emitter.ASK");
        COMMANDS.put("LOCATE", "com.deveryware.emitter.LOCATE");
        COMMANDS.put("SET", "com.deveryware.emitter.SET");
        COMMANDS.put("GET", "com.deveryware.emitter.GET");
        COMMANDS.put("NOTIFY", "com.deveryware.emitter.NOTIFY");
        COMMANDS.put("LOCATE_AT", "com.deveryware.emitter.LOCATE_AT");
    }

    public static class CellIdInfo {
        public CellLocation cellLoc;

        public List<NeighboringCellInfo> cellInfo;

        public String operator;

        public int signalStrength = -1;

        public int networkType = -1;
    }

    public static class WifiInfo {
        public android.net.wifi.WifiInfo wifiInfo;

        public List<ScanResult> scanResult;
    }

    public static class RequestInfo {

        public boolean isWifiAvailable;

        public long startedAt;

        public String pid;

        public boolean returnLastPos;

        public AlarmParameter alarmParameter;

        public String cmdid;

        public int cmdack;

        public String cmdtext;

        public boolean displayVersion;

        public boolean radius;

        public String text1;

        public String text2;

        public boolean apc;

        public int retry = 5;

        public Location defaultLocation;

        public boolean isLocationOptional;
    }

    public static class LocationInfo {
        public GpsStatus gpsStatus;

        public String provider;

        public boolean isSeamless;

        public boolean isDeveryware;
    }

    public static class IpInfo {
        public List<IPInfos> ipInfos = new ArrayList<IPInfos>();
    }

    public static class PhoneInfo {
        public int level = -1;

        public int status = -1;

        public int scale = -1;

        public int plugged = -1;

        public NetworkInfo network;
    }

    public final static String query(LocationInfo locationInfo, Location location, RequestInfo requestInfo,
            CellIdInfo cellIdInfo, WifiInfo wifiInfo, PhoneInfo phoneInfo, int applicationVersion, String firmwareVersion,
            IpInfo ipInfo)
    {
        final long now = System.currentTimeMillis();
        final Query q = new Query();
        q.setTime(now);

        if (requestInfo != null) {
            q.setIdentity(requestInfo.pid);
        }

        if (location == null) {
            location = requestInfo.defaultLocation;
        }

        if (locationInfo != null && location != null) {

            int nbSatellites = 0;
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER) && locationInfo.gpsStatus != null) {
                for (@SuppressWarnings("unused")
                GpsSatellite satellite : locationInfo.gpsStatus.getSatellites()) {
                    nbSatellites++;
                }
            }

            /*
             * Issue 23937: GPS Provider : android.location.Location.getTime() returns wrong UTC timestamp on 4.0.3 / Nexus S
             * http://code.google.com/p/android/issues/detail?id=23937
             */
            if (location.getTime() > now) {
                location.setTime(now);
            }

            final GpsLocation gpsLocation = new GpsLocation();
            gpsLocation.setAccuracy(location.getAccuracy());
            gpsLocation.setAltitude(location.getAltitude());
            gpsLocation.setBearing(location.getBearing());
            gpsLocation.setLatitude(location.getLatitude());
            gpsLocation.setLongitude(location.getLongitude());
            gpsLocation.setNbSatellites(nbSatellites);
            gpsLocation.setSpeed(location.getSpeed());
            gpsLocation.setTime(location.getTime());
            q.setGpsLocation(gpsLocation);

            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                gpsLocation.setValidity(Validity.VALID);
            }
        }

        q.setPhone(new Phone());
        q.getPhone().setApc(APCState.ON);
        q.getPhone().setRssi(0);
        q.getPhone().setNetwork(Network.UNAVAILABLE);
        
        if (requestInfo != null && requestInfo.apc == false) {
            q.getPhone().setApc(APCState.OFF);
        }

        if (cellIdInfo != null && cellIdInfo.signalStrength > -1) {
            q.getPhone().setRssi(cellIdInfo.signalStrength);
            if (phoneInfo.network != null && phoneInfo.network.isAvailable()) {
                q.getPhone().setNetwork(Network.AVAILABLE);
            }
        }

        if (phoneInfo != null && phoneInfo.level > -1) {
            q.getPhone().setBatteryPercent(phoneInfo.level * 100 / phoneInfo.scale);

            if (phoneInfo.status == BatteryManager.BATTERY_STATUS_FULL) {
                q.getPhone().setBatteryState(BatteryState.FULL);
            } else if (phoneInfo.status == BatteryManager.BATTERY_STATUS_CHARGING) {
                q.getPhone().setBatteryState(BatteryState.CHARGE);
            } else {
                q.getPhone().setBatteryState(BatteryState.DISCHARGE);
            }

            /* low battery alarm */
            if (q.getPhone().getBatteryPercent() < 15 && q.getAlarmParameter() == null) {
                q.setAlarmParameter(new AlarmParameter(4, "12"));
            }
        }

        if (requestInfo != null && requestInfo.alarmParameter != null) {
            q.setAlarmParameter(requestInfo.alarmParameter);
        }

        if (requestInfo != null && requestInfo.cmdid != null) {
            final CommandResponse commandResponse = new CommandResponse();
            commandResponse.setAnswer(requestInfo.cmdtext);
            commandResponse.setId(requestInfo.cmdid);
            commandResponse.setAck(CommandState.stateOf(requestInfo.cmdack));
            q.setCommandResponse(commandResponse);
        }

        if (requestInfo != null && requestInfo.displayVersion) {
            q.setFirmware(firmwareVersion);
            q.setVersion(applicationVersion);
        }

        if (requestInfo != null && requestInfo.returnLastPos) {
            q.setServerCommandDesc(new ServerCommandDesc()); // LSTPOS is default
        }

        if (requestInfo != null && (requestInfo.text1 != null || requestInfo.text2 != null)) {
            final Inputs inputs = new Inputs();
            inputs.setTxt1(requestInfo.text1);
            inputs.setTxt2(requestInfo.text2);
            q.setInputs(inputs);
        }

        /*
         * With this version, we do not support CdmaCellLocation (available only with android > 1.6)
         */
        if (cellIdInfo != null && cellIdInfo.cellLoc != null && cellIdInfo.cellLoc instanceof GsmCellLocation) {
            /*
             * without SIM card on the phone we have lac: -1, cid: -1 :-o with my HTC G1 (android 1.6)
             */
            GsmCellLocation fixMe = (GsmCellLocation) cellIdInfo.cellLoc;
            if (fixMe.getCid() != -1 && fixMe.getLac() != -1) {

                final Cell currentCell = new Cell();
                final Type type = convertFromAndroidNetworkType(cellIdInfo.networkType);
                currentCell.setCid(fixMe.getCid());
                currentCell.setLac(fixMe.getLac());
                currentCell.setOperator(cellIdInfo.operator);
                currentCell.setRssi(cellIdInfo.signalStrength);
                currentCell.setNetworkType(type);
                q.getCells().add(currentCell);

                /*
                 * traitement des cellules voisines. Dans le cadre de cellules UMTS, il n'est pas possible de remonter le cid des
                 * cellules voisine. Il existe une solution en passant par getPsc() mais disponible qu'en API 5. Pour le cdma nous
                 * avons une solution, http://developer.android.com/reference/android/telephony/cdma/CdmaCellLocation.html. Pour
                 * l'utms il faudra passer par le getPsc().
                 */
                if (cellIdInfo.cellInfo != null) {
                    for (NeighboringCellInfo neighboringCellInfo : cellIdInfo.cellInfo) {
                        final Cell cell = new Cell();
                        cell.setCid(neighboringCellInfo.getCid());
                        cell.setLac(fixMe.getLac());
                        cell.setOperator(cellIdInfo.operator);
                        cell.setRssi(neighboringCellInfo.getRssi());
                        cell.setNetworkType(type);
                        q.getCells().add(cell);
                    }
                }
            }
        }

        if (wifiInfo != null && wifiInfo.wifiInfo != null && wifiInfo.scanResult != null) {

            for (ScanResult scan : wifiInfo.scanResult) {
                final Wifi wifi = new Wifi();
                wifi.setBssid(scan.BSSID);
                wifi.setCapabilities(scan.capabilities);
                wifi.setFrequency(scan.frequency);
                wifi.setLevel(scan.level);
                wifi.setSsid(scan.SSID);
                q.getWifis().add(wifi);
            }
        }

        if (ipInfo.ipInfos != null && ipInfo.ipInfos.size() > 0) {
            q.getIpInfos().addAll(ipInfo.ipInfos);
        }

        return query.make(q);
    }

    static Type convertFromAndroidNetworkType(int networkType)
    {
        Type deverywareNetworkType = Type.UNKNOWN;
        switch (networkType) {
        case (TelephonyManager.NETWORK_TYPE_EVDO_0):
        case (TelephonyManager.NETWORK_TYPE_EVDO_A):
        case (TelephonyManager.NETWORK_TYPE_CDMA):
            deverywareNetworkType = Type.CDMA; // cdma2000
            break;
        case (TelephonyManager.NETWORK_TYPE_EDGE):
        case (TelephonyManager.NETWORK_TYPE_GPRS):
            deverywareNetworkType = Type.GPRS; // gsm
            break;
        case (TelephonyManager.NETWORK_TYPE_UMTS):
            deverywareNetworkType = Type.UMTS; // wcdma
            break;
        case (TelephonyManager.NETWORK_TYPE_UNKNOWN):
        default:
            deverywareNetworkType = Type.UNKNOWN; // unknown
            break;
        }
        return deverywareNetworkType;
    }

    public static Intent[] extractCommands(String command)
    {
        final ArrayList<Intent> r = new ArrayList<Intent>();
        try {
            final Result rr = result.parse(command);

            for (Command cmd : rr.getCommands()) {
                Intent intent = new Intent(COMMANDS.get(cmd.getAction()));
                intent.putExtra(GiftClient.CMD_ID, cmd.getId());
                intent.putExtra(GiftClient.CMD_PARAMS, cmd.getParams());
                r.add(intent);
            }

            final com.deveryware.gift.data.Location lastPos = rr.getLastPosition();
            if (lastPos != null) {
                final Location location = new Location("deveryware");
                location.setLatitude(lastPos.getLatitude());
                location.setLongitude(lastPos.getLongitude());
                location.setTime(lastPos.getTime());
                final Intent intent = new Intent(Constants.LASTPOS_ACTION);
                intent.putExtra("api", "origin");
                intent.putExtra("deveryware", location);
                r.add(intent);
            }

        } catch (Exception e) {
            Log.e(Constants.NAME, "error during parsing commands", e);
        }

        return r.toArray(new Intent[] {});
    }

    public static final String translateResult(Context context, String r)
    {
        try {
            final Result rr = result.parse(r);

            if (!rr.getCommands().isEmpty() || rr.getLastPosition() != null) {
                return r;
            }

            if (rr.getResponse() == Response.SUCCESS) {
                return context.getString(R.string.update_location_success);
            }

            if (rr.getResponse() == Response.WARNING) {
                return context.getString(R.string.update_location_warning);
            }

            if (rr.getResponse() == Response.ERROR) {
                return context.getString(R.string.update_location_error);
            }

            if (TransportClient.RESULT_EXTRA.equals(r)) {
                return context.getString(R.string.update_location_unknown);
            }

        } catch (Exception e) {
            return context.getString(R.string.awaiting_transfer);
        }

        return r;
    }
}
