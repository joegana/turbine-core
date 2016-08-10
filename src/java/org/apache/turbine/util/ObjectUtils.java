package org.apache.turbine.util;


/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * This is where common Object manipulation routines should go.
 *
 * @author <a href="mailto:nissim@nksystems.com">Nissim Karpenstein</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id$
 */
public abstract class ObjectUtils
{
    /**
     * Converts a map to a byte array for storage/serialization.
     *
     * @param map The Map to convert.
     *
     * @return A byte[] with the converted Map.
     *
     * @exception Exception A generic exception.
     */
	public static byte[] serializeMap(Map<String, Object> map)
            throws Exception
    {
        String key = null;
        Object value = null;
        byte[] byteArray = null;

        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            key = entry.getKey();
            value = entry.getValue();
            if (! (value instanceof Serializable))
            {
                throw new Exception("Could not serialize, value is not serializable:" + value);
            }
        }

        ByteArrayOutputStream baos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream out = null;
        try
        {
            // These objects are closed in the finally.
            baos = new ByteArrayOutputStream();
            bos  = new BufferedOutputStream(baos);
            out  = new ObjectOutputStream(bos);

            out.writeObject(map);
            out.flush();
            bos.flush();

            byteArray = baos.toByteArray();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
            if (bos != null)
            {
                bos.close();
            }
            if (baos != null)
            {
                baos.close();
            }
        }
        return byteArray;
    }

    /**
     * Deserializes a single object from an array of bytes.
     *
     * @param objectData The serialized object.
     *
     * @return The deserialized object, or <code>null</code> on failure.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] objectData)
    {
        T object = null;

        if (objectData != null)
        {
            // These streams are closed in finally.
            ObjectInputStream in = null;
            ByteArrayInputStream bin = new ByteArrayInputStream(objectData);
            BufferedInputStream bufin = new BufferedInputStream(bin);

            try
            {
                in = new ObjectInputStream(bufin);

                // If objectData has not been initialized, an
                // exception will occur.
                object = (T)in.readObject();
            }
            catch (Exception e)
            {
                // ignore
            }
            finally
            {
                try
                {
                    if (in != null)
                    {
                        in.close();
                    }

                    bufin.close();
                    bin.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        return object;
    }
}
