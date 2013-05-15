package com.tombarrasso.android.deviceinfo;

/*
 * Copyright 2013 Thomas Barrasso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Java Packages
import java.lang.reflect.Method;

public final class SystemPropertiesProxy {
	
	/**
	 * This class cannot be initialized!
	 */
	private SystemPropertiesProxy() { }
	
	// SystemProperties class.
	private static final String SYS_PROP = "android.os.SystemProperties";
	
	// Cached SystemProperties class.
	private static Class<?> mClass = null;
	
	/**
	 * @return The class for {@link SystemProperties}.
	 */
	private static final Class<?> getSystemPropertiesClass()
	{
		if (mClass != null) return mClass;
		
		try {
	        mClass = Class.forName(SYS_PROP);
	    } catch (ClassNotFoundException c) { }
        
        return mClass;
	}
	
	// Cached Methods.
	private static Method mGet		= null,
						  mGetDef	= null,
						  mGetInt	= null,
						  mGetLong	= null,
						  mGetBool	= null;

    /**
     * Get the value for the given key.
     * @return an empty string if the key isn't found
     */
    public static final String get(final String key)
    {
		String ret = "";
		
		try {
			final Class<?> clazz = getSystemPropertiesClass();
			if (clazz == null) return ret;
			if (null == mGet) {
				mGet = clazz.getDeclaredMethod("get", new Class[] { String.class });
				mGet.setAccessible(true);
			}
			ret = (String) mGet.invoke(clazz, new Object[] { key });
		} catch(Throwable e) {
			ret = "";
		}
		
		return ret;
    }
    
    /**
     * Get the value for the given key.
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     */
    public static final String get(final String key, final String def)
    {
		String ret = def;
		
		try {
			final Class<?> clazz = getSystemPropertiesClass();
			if (clazz == null) return ret;
			if (null == mGetDef) {
				mGetDef = clazz.getDeclaredMethod("get", new Class[] { String.class, String.class });
				mGetDef.setAccessible(true);
			}
			
			ret = (String) mGetDef.invoke(clazz,
				new Object[] { key, def });
		} catch(Throwable e) {
			ret = def;
		}

        return ret;
    }

    /**
     * Get the value for the given key, and return as an integer.
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as an integer, or def if the key isn't found or
     *         cannot be parsed
     */
    public static final Integer getInt(final String key, final int def)
	{
		Integer ret = def;
		
		try {
			final Class<?> clazz = getSystemPropertiesClass();
			if (clazz == null) return ret;
			if (null == mGetInt) {
				mGetInt = clazz.getDeclaredMethod("getInt",
					new Class[] { String.class, Integer.TYPE });
				mGetInt.setAccessible(true);
			}
			
			ret = (Integer) mGetInt.invoke(clazz,
				new Object[] { key, def });
		} catch(Throwable e ) {
			ret = def;
		}

        return ret;
    }

    /**
     * Get the value for the given key, and return as a long.
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a long, or def if the key isn't found or
     *         cannot be parsed
     */
    public static final Long getLong(final String key, final long def)
	{
		Long ret = def;
		
		try {
			final Class<?> clazz = getSystemPropertiesClass();
			if (clazz == null) return ret;
			if (null == mGetLong) {
				mGetLong = clazz.getDeclaredMethod("getLong",
					new Class[] { String.class, Long.TYPE });
				mGetLong.setAccessible(true);
			}
			
			ret = (Long) mGetLong.invoke(clazz,
				new Object[] { key, def });
		} catch(Throwable e ) {
			ret = def;
		}

        return ret;
    }

    /**
     * Get the value for the given key, returned as a boolean.
     * Values 'n', 'no', '0', 'false' or 'off' are considered false.
     * Values 'y', 'yes', '1', 'true' or 'on' are considered true.
     * (case insensitive).
     * If the key does not exist, or has any other value, then the default
     * result is returned.
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is
     *         not able to be parsed as a boolean.
     */
    public static final Boolean getBoolean(final String key, final boolean def)
    {
		Boolean ret = def;
		
		try {
			final Class<?> clazz = getSystemPropertiesClass();
			if (clazz == null) return ret;
			if (null == mGetBool) {
				mGetBool = clazz.getDeclaredMethod("getBoolean",
					new Class[] { String.class, Boolean.TYPE });
				mGetBool.setAccessible(true);
			}
			
			ret = (Boolean) mGetBool.invoke(clazz,
				new Object[] { key, def });
		} catch(Throwable e ) {
			ret = def;
		}

        return ret;
    }
}