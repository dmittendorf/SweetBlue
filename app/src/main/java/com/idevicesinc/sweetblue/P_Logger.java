package com.idevicesinc.sweetblue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.idevicesinc.sweetblue.utils.Utils;
import com.idevicesinc.sweetblue.utils.Utils_Reflection;
import com.idevicesinc.sweetblue.utils.UuidNameMap;
import com.idevicesinc.sweetblue.utils.UuidNameMap_ListWrapper;


@SuppressLint("UseSparseArrays")
final class P_Logger
{

	private final static String MAIN = "MAIN";
	private final static String UPDATE = "UPDATE";
	private String[] m_debugThreadNamePool;
	private int m_poolIndex = 0;
	private final Map<Integer, String> m_threadNames = new ConcurrentHashMap<>();
	private HashMap<Integer, String> m_gattStatusCodes = null;
	private HashMap<Integer, String> m_gattConnStates = null;
	private HashMap<Integer, String> m_gattBleStates = null;
	private HashMap<Integer, String> m_gattBondStates = null;
	private HashMap<Integer, String> m_unbondReasonCodes = null;
	private boolean m_enabled;
	private final UuidNameMap_ListWrapper m_nameMap;
	private SweetLogger m_logger = null;
	private BleManager m_mgr;


	public P_Logger(final BleManager manager, String[] debugThreadNamePool, List<UuidNameMap> debugUuidNameDicts, boolean enabled, SweetLogger logger)
	{
		m_mgr = manager;
		m_logger = logger;
		m_debugThreadNamePool = debugThreadNamePool;
		m_nameMap = new UuidNameMap_ListWrapper(debugUuidNameDicts);
		m_enabled = enabled;
	}

	public final void setBleManager(BleManager mgr)
	{
		m_mgr = mgr;
	}

	public final void printBuildInfo()
	{
		if (!m_enabled) return;

		int level = Log.DEBUG;

		for (Field field : Build.class.getFields())
		{
			String fieldName = field.getName();
			String fieldValue = Utils_Reflection.fieldStringValue(field);

			this.log(level, null, fieldName + ": " + fieldValue);
		}
	}

	public final boolean isEnabled()
	{
		return m_enabled;
	}

	public final synchronized String getThreadName(int threadId)
	{
		String threadName = null;

		if (m_threadNames != null)
		{
			threadName = m_threadNames.get(threadId);

			if (threadName == null)
			{
				threadName = m_debugThreadNamePool[m_poolIndex % m_debugThreadNamePool.length];
				threadName = threadName + "(" + threadId + ")";

				m_threadNames.put(threadId, threadName);

				m_poolIndex++;
			}
		}

		return threadName == null ? "" : threadName;
	}

	public final void setMainThread(int threadId)
	{
		m_threadNames.put(threadId, MAIN + "(" + threadId + ")");
	}

	public final void setUpdateThread(int threadId)
	{
		m_threadNames.put(threadId, UPDATE + "(" + threadId + ")");
	}


	// *** Native log methods

	public final void log_native(int level, String macAddress, String message)
	{
		if( !m_enabled )  return;

		if ( !m_mgr.m_config.logOptions.nativeEnabled(level) )	return;

		StackTraceElement trace = getSoonestTrace();
		String className = trace.getClassName();
		String[] className_split = className.split("\\.");
		className = className_split[className_split.length-1] + " [Native]";
		log_private(level, className, macAddress, message, trace);
	}

	public final void log_native(int level, String tag, String macAddress, String message)
	{
		if( !m_enabled )  return;

		if ( !m_mgr.m_config.logOptions.nativeEnabled(level) )	return;

		StackTraceElement trace = getSoonestTrace();
		log_private(level, tag + " [Native]", macAddress, message, trace);
	}
	
	public final void log_status_native(String macAddress, int gattStatus)
	{
		log_status_native(macAddress, gattStatus, "");
	}
	
	public final void log_status_native(String macAddress, int gattStatus, String message)
	{
		if( !m_enabled )  return;

		int level = Utils.isSuccess(gattStatus) ? Log.INFO : Log.WARN;
		message = gattStatus(gattStatus) + " " + message;
		
		log_native(level, macAddress, message);
	}

	public final void d_native(String tag, String message)
	{
		log_native(Log.DEBUG, tag, message);
	}

	public final void i_native(String tag, String message)
	{
		log_native(Log.INFO, tag, message);
	}

	public final void e_native(String tag, String message)
	{
		log_native(Log.ERROR, tag, message);
	}

	public final void w_native(String tag, String message)
	{
		log_native(Log.WARN, tag, message);
	}

	public final void d_native(String message)
	{
		log_native(Log.DEBUG, null, message);
	}

	public final void i_native(String message)
	{
		log_native(Log.INFO, null, message);
	}

	public final void e_native(String message)
	{
		log_native(Log.ERROR, null, message);
	}

	public final void w_native(String message)
	{
		log_native(Log.WARN, null, message);
	}


	// *** Sweetblue log methods

	public final void log(int level, String macAddress, String message)
	{
		if( !m_enabled )  return;

		if ( !m_mgr.m_config.logOptions.sweetBlueEnabled(level) )	return;

		StackTraceElement trace = getSoonestTrace();
		String className = trace.getClassName();
		String[] className_split = className.split("\\.");
		className = className_split[className_split.length-1];
		log_private(level, className, macAddress, message, trace);
	}
	
	public final void log(int level, String tag, String macAddress, String message)
	{
		if( !m_enabled )  return;

		if ( !m_mgr.m_config.logOptions.sweetBlueEnabled(level) )	return;
		
		StackTraceElement trace = getSoonestTrace();
		log_private(level, tag, macAddress, message, trace);
	}

	public final void d(String tag, String message)
	{
		log(Log.DEBUG, null, tag, message);
	}
	
	public final void i(String tag, String message)
	{
		log(Log.INFO, null, tag, message);
	}

	public final void e(String tag, String message)
	{
		log(Log.ERROR, null, tag, message);
	}
	
	public final void w(String tag, String message)
	{
		log(Log.WARN, null, tag, message);
	}
	
	public final void d(String message)
	{
		log(Log.DEBUG, null, message);
	}
	
	public final void i(String message)
	{
		log(Log.INFO, null, message);
	}

	public final void e(String message)
	{
		log(Log.ERROR, null, message);
	}
	
	public final void w(String message)
	{
		log(Log.WARN, null, message);
	}
	
	public final String gattConn(int code)
	{
		String name = "NO_NAME";
		
		if( m_gattConnStates == null && m_enabled )
		{
			initConnStates();
		}
		
		if( m_gattConnStates != null )
		{
			String actualName = m_gattConnStates.get(code);
			name = actualName != null ? actualName : name;
		}
		
		return name+"("+code+")";
	}

	public final String gattStatus(int code)
	{
		String errorName = "GATT_STATUS_NOT_APPLICABLE";
		
		if( m_gattStatusCodes == null && m_enabled )
		{
			initGattStatusCodes();
		}
		
		if( m_gattStatusCodes != null )
		{
			String actualErrorName = m_gattStatusCodes != null ? m_gattStatusCodes.get(code) : null;
			errorName = actualErrorName != null ? actualErrorName : errorName;
		}
		
		return errorName+"("+code+")";
	}
	
	public final String gattBleState(int code)
	{
		String name = "NO_NAME";
		
		if( m_gattBleStates == null && m_enabled )
		{
			initGattBleStates();
		}
		
		if( m_gattBleStates != null )
		{
			String actualName = m_gattBleStates.get(code);
			name = actualName != null ? actualName : name;
		}
		
		return name+"("+code+")";
	}
	
	public final String gattUnbondReason(int code)
	{
		String name = "NO_NAME";
		
		if( m_unbondReasonCodes == null && m_enabled )
		{
			initUnbondReasonCodes();
		}
		
		if( m_unbondReasonCodes != null )
		{
			String actualName = m_unbondReasonCodes.get(code);
			name = actualName != null ? actualName : name;
		}
		
		return name+"("+code+")";
	}
	
	public final String gattBondState(int code)
	{
		String name = "NO_NAME";
		
		if( m_gattBondStates == null && m_enabled )
		{
			initGattBondStates();
		}
		
		if( m_gattBondStates != null )
		{
			String actualName = m_gattBondStates.get(code);
			name = actualName != null ? actualName : name;
		}
		
		return name+"("+code+")";
	}

	public final String descriptorName(UUID uuid)
	{
		return uuidName(uuidToString(uuid), "descriptor");
	}
	
	public final String charName(UUID uuid)
	{
		return uuidName(uuidToString(uuid), "char");
	}
	
	public final String serviceName(UUID uuid)
	{
		return uuidName(uuidToString(uuid), "service");
	}
	
	public final String uuidName(UUID uuid)
	{
		return uuidName(uuidToString(uuid));
	}
	
	public final String uuidName(String uuid)
	{
		return uuidName(uuid, null);
	}
	
	public final String uuidName(String uuid, String type)
	{
		String debugName = m_nameMap.getUuidName(uuid);
		
		return (type == null ? debugName : type+"="+debugName);
	}

	final <T> void checkPlease(final T please_nullable, final Class<T> please_class)
	{
		final Class<? extends Object> class_Listener = please_class.getEnclosingClass();
		
		if( please_nullable == null )
		{
			w("WARNING: The " +please_class.getSimpleName() + " returned from " +class_Listener.getSimpleName() +".onEvent() is null. Consider returning a valid instance using static constructor methods.");
		}
	}



	private void log_private(int level, String tag, String macAddress, String message, StackTraceElement trace)
	{
		message = prefixMessage(trace.getMethodName(), macAddress, message);
		if (m_logger != null)
		{
			m_logger.onLogEntry(level, tag, message);
		}
		else
		{
			Log.println(level, tag, message);
		}
	}

	private StackTraceElement getSoonestTrace()
	{
		StackTraceElement[] trace = new Exception().getStackTrace();
		return getSoonestTrace(trace);
	}

	private StackTraceElement getSoonestTrace(StackTraceElement[] trace)
	{
		for(int i = 0; i < trace.length; i++ )
		{
			if( !trace[i].getClassName().equals(this.getClass().getName()) )
			{
				return trace[i];
			}
		}

		return null;
	}

	private String prefixMessage(String methodName, String macAddress, String message)
	{
		String threadName = getThreadName(Process.myTid());
		StringBuilder b = new StringBuilder();
		b.append(threadName).append(" ").append(methodName).append("() ");
		if (!TextUtils.isEmpty(macAddress))
		{
			b.append("[").append(macAddress).append("] ");
		}
		b.append("- ").append(message);

		return b.toString();
	}

	private String uuidToString(UUID uuid_nullable)
	{
		return uuid_nullable == null ? "null-uuid" : uuid_nullable.toString();
	}

	private synchronized void initGattStatusCodes()
	{
		if( m_gattStatusCodes != null )  return;

		m_gattStatusCodes = new HashMap<>();

		initFromReflection(BluetoothGatt.class, "GATT_", m_gattStatusCodes);
		initFromReflection(BleDeviceConfig.class, "GATT_", m_gattStatusCodes);
		initFromReflection(BleStatuses.class, "GATT_", m_gattStatusCodes);
	}

	private synchronized void initGattBleStates()
	{
		if( m_gattBleStates != null )  return;

		m_gattBleStates = new HashMap<>();

		initFromReflection(BluetoothAdapter.class, "STATE_", m_gattBleStates);

		m_gattBleStates.put(BluetoothAdapter.ERROR, "ERROR");
	}

	private synchronized void initUnbondReasonCodes()
	{
		if( m_unbondReasonCodes != null )  return;

		m_unbondReasonCodes = new HashMap<>();

		initFromReflection(BluetoothDevice.class, "UNBOND_REASON_", m_unbondReasonCodes);
		initFromReflection(BleStatuses.class, "BOND_FAIL_REASON", m_unbondReasonCodes);
	}

	private synchronized void initGattBondStates()
	{
		if( m_gattBondStates != null )  return;

		m_gattBondStates = new HashMap<>();

		initFromReflection(BluetoothDevice.class, "BOND_", m_gattBondStates);
	}

	private synchronized void initConnStates()
	{
		if( m_gattConnStates != null )  return;

		m_gattConnStates = new HashMap<>();

		initFromReflection(BluetoothProfile.class, "STATE_", m_gattConnStates);
	}

	private static void initFromReflection(Class<?> clazz, String fieldSuffix, HashMap<Integer, String> map)
	{
		for( Field field : clazz.getFields() )
		{
			String fieldName = field.getName();

			if( !fieldName.contains(fieldSuffix) )  continue;

			Integer fieldValue = -1;

			try {
				fieldValue = field.getInt(null);
			} catch (IllegalAccessException e) {
//				e.printStackTrace();
			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
			}

			if( !map.containsKey(fieldValue) )
			{
				map.put(fieldValue, fieldName);
			}
		}
	}
}
