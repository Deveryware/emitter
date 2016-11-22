/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.library;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmParameter extends com.deveryware.gift.data.AlarmParameter implements Parcelable {

    public AlarmParameter(Parcel arg0)
    {
        setCause(Cause.causeOf(arg0.readInt()));
        setAlarm(Alarm.alarmOf(arg0.readString()));
        setParam1(arg0.readString());
        setParam2(arg0.readString());
    }

    public AlarmParameter(int cause, String alarm)
    {
        this(cause, alarm, null, null);
    }

    public AlarmParameter(int cause, String alarm, String param1, String param2)
    {
        setCause(Cause.causeOf(cause));
        setAlarm(Alarm.alarmOf(alarm));

        if (param1 != null) {
            setParam1(param1);
        } else {
            setParam1("");
        }

        if (param2 != null) {
            setParam2(param2);
        } else {
            setParam2("");
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1)
    {
        arg0.writeInt(getCause().getCause());
        arg0.writeString(getAlarm().getAlarm());
        arg0.writeString(getParam1());
        arg0.writeString(getParam2());
    }

    public static final Creator<AlarmParameter> CREATOR = new Creator<AlarmParameter>() {

        @Override
        public AlarmParameter createFromParcel(Parcel arg0)
        {
            return new AlarmParameter(arg0.readInt(), arg0.readString(), arg0.readString(), arg0.readString());
        }

        @Override
        public AlarmParameter[] newArray(int arg0)
        {
            return new AlarmParameter[arg0];
        }
    };

}
