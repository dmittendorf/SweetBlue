package com.idevicesinc.sweetblue;


import com.idevicesinc.sweetblue.listeners.DeviceStateListener;
import com.idevicesinc.sweetblue.listeners.DeviceStateListener.StateEvent;
import com.idevicesinc.sweetblue.listeners.P_EventFactory;


public final class P_DeviceStateTracker extends P_StateTracker
{

    DeviceStateListener mStateListener;
    private final BleDevice mDevice;
    private final boolean mShortTermReconnect;
    private boolean mSyncing;


    public P_DeviceStateTracker(BleDevice device, boolean forShortTermReconnect)
    {
        super(BleDeviceState.VALUES(), !forShortTermReconnect);
        mDevice = device;
        mShortTermReconnect = forShortTermReconnect;
    }

    public final void setListener(DeviceStateListener listener)
    {
        mStateListener = listener;
    }

    public final void sync(P_DeviceStateTracker otherTracker)
    {
        mSyncing = true;
        copy(otherTracker);
        mSyncing = false;
    }


    @Override final void onStateChange(final int oldStateBits, final int newStateBits, final int intentMask, final int gattStatus)
    {
        if( mDevice.isNull() )		return;
        if( mSyncing )				return;

        if( mStateListener != null )
        {
            mDevice.getManager().mPostManager.postCallback(new Runnable()
            {
                @Override public void run()
                {
                    StateEvent event = P_EventFactory.newDeviceStateEvent(mDevice, oldStateBits, newStateBits, intentMask, gattStatus);
                    mStateListener.onEvent(event);
                }
            });
        }

        if( !mShortTermReconnect && mDevice.getManager().mDefaultStateListener != null )
        {
            mDevice.getManager().mPostManager.postCallback(new Runnable()
            {
                @Override public void run()
                {
                    StateEvent event = P_EventFactory.newDeviceStateEvent(mDevice, oldStateBits, newStateBits, intentMask, gattStatus);
                    mDevice.getManager().mDefaultStateListener.onEvent(event);
                }
            });
        }
    }

    @Override public final String toString()
    {
        return super.toString(BleDeviceState.VALUES());
    }
}
