package com.idevicesinc.sweetblue;


import com.idevicesinc.sweetblue.utils.BitwiseEnum;


/**
 * Enumeration used to dictate what log statements you'd like to have printed via SweetBlue's logging system.
 */
public enum LogType implements BitwiseEnum
{

    /**
     * Print all info level log statements
     */
    INFO,

    /**
     * Print all debug level log statements
     */
    DEBUG,

    /**
     * Print all warn level log statements
     */
    WARN,

    /**
     * Print all error level log statements
     */
    ERROR,

    /**
     * Print all log statements which have to do with native android bluetooth callbacks
     */
    NATIVE;




    @Override public int or(BitwiseEnum state)
    {
        return this.bit() | state.bit();
    }

    @Override public int or(int bits)
    {
        return this.bit() | bits;
    }

    @Override public int bit()
    {
        return 0x1 << ordinal();
    }

    @Override public boolean overlaps(int mask)
    {
        return (bit() & mask) != 0x0;
    }

    public static int allLogs()
    {
        int mask = 0;
        for (LogType type : values())
        {
            mask = type.or(mask);
        }
        return mask;
    }

    public static int logTypes(LogType... types)
    {
        if (types == null || types.length == 0)
        {
            return 0;
        }
        int mask = 0;
        for (LogType type : types)
        {
            mask = type.or(mask);
        }
        return mask;
    }

}
