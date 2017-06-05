package com.idevicesinc.sweetblue;


import android.util.Log;

/**
 * Class used to specify the logging options in SweetBlue. There are two types of logs SweetBlue will print. The first are SweetBlue
 * logs that have to do with SweetBlue logic, the other are "native" logs, which print when SweetBlue receives any callbacks from the
 * native stack. Each of these two types can then specify the log level to be printed (Use {@link Level} to pick with ones you would like
 * to see).
 *
 * You can also use {@link #OFF} to shut all logging off, or {@link #ALL_ON} to turn all logging on for convenience.
 */
public final class LogOptions
{



    private int m_sweetBlueMask = 0;
    private int m_nativeMask = 0;


    public LogOptions enableSweetBlueLogs(Level... levels)
    {
        if (levels != null && levels.length > 0)
        {
            for (Level l : levels)
            {
                m_sweetBlueMask |= l.m_bit;
            }
        }
        return this;
    }

    public LogOptions enableNativeLogs(Level... levels)
    {
        if (levels != null && levels.length > 0)
        {
            for (Level l : levels)
            {
                m_nativeMask |= l.m_nativeInt;
            }
        }
        return this;
    }



    final boolean enabled()
    {
        return sweetBlueEnabled() || nativeEnabled();
    }

    final boolean sweetBlueEnabled()
    {
        return m_sweetBlueMask != 0;
    }

    final boolean nativeEnabled()
    {
        return m_nativeMask != 0;
    }

    final boolean nativeEnabled(int logLevel)
    {
        final Level l = Level.fromNative(logLevel);
        return (m_nativeMask & l.nativeInt()) != 0x0;
    }

    final boolean sweetBlueEnabled(int logLevel)
    {
        final Level l = Level.fromNative(logLevel);
        return (m_sweetBlueMask & l.nativeInt()) != 0x0;
    }


    public enum Level
    {

        INFO(1 << 0, Log.INFO),
        DEBUG(1 << 1, Log.DEBUG),
        WARN(1 << 2, Log.WARN),
        ERROR(1 << 3, Log.ERROR);

        private final int m_bit;
        private final int m_nativeInt;
        private static Level[] VALUES;


        Level(int bit, int nativeInt)
        {
            m_bit = bit;
            m_nativeInt = nativeInt;
        }

        public int bit()
        {
            return m_bit;
        }

        public int nativeInt()
        {
            return m_nativeInt;
        }

        public static Level fromNative(int nativeInt)
        {
            for (Level l : VALUES())
            {
                if (l.nativeInt() == nativeInt)
                {
                    return l;
                }
            }
            return INFO;
        }

        public static Level[] VALUES()
        {
            if (VALUES == null)
            {
                VALUES = values();
            }
            return VALUES;
        }

    }

    /**
     * Static instance of {@link LogOptions}, which shuts off all logging.
     */
    public static LogOptions OFF = new LogOptions();

    /**
     * Static instance of {@link LogOptions} which enables all possible logging types.
     */
    public static LogOptions ALL_ON = new LogOptions().enableSweetBlueLogs(Level.VALUES()).enableNativeLogs(Level.VALUES());

}
