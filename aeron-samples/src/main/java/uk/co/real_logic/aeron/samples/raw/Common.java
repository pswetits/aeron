/*
 * Copyright 2014 - 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.aeron.samples.raw;

import uk.co.real_logic.aeron.driver.NioSelectedKeySet;
import uk.co.real_logic.agrona.IoUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.net.StandardSocketOptions;
import java.nio.channels.*;

import static uk.co.real_logic.aeron.driver.Configuration.MTU_LENGTH_DEFAULT;

public class Common
{
    public static final int NUM_MESSAGES = 10_000;

    public static final int PONG_PORT = 40123;
    public static final int PING_PORT = 50123;

    static final Field SELECTED_KEYS_FIELD;
    static final Field PUBLIC_SELECTED_KEYS_FIELD;

    static
    {
        Field selectKeysField = null;
        Field publicSelectKeysField = null;

        try
        {
            final Class<?> clazz = Class.forName("sun.nio.ch.SelectorImpl", false, ClassLoader.getSystemClassLoader());

            if (clazz.isAssignableFrom(Selector.open().getClass()))
            {
                selectKeysField = clazz.getDeclaredField("selectedKeys");
                selectKeysField.setAccessible(true);

                publicSelectKeysField = clazz.getDeclaredField("publicSelectedKeys");
                publicSelectKeysField.setAccessible(true);
            }
        }
        catch (final Exception ignore)
        {
        }

        SELECTED_KEYS_FIELD = selectKeysField;
        PUBLIC_SELECTED_KEYS_FIELD = publicSelectKeysField;
    }

    public static void init(final DatagramChannel channel) throws IOException
    {
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    }

    public static NioSelectedKeySet keySet(final Selector selector)
    {
        NioSelectedKeySet tmpSet = null;

        if (null != PUBLIC_SELECTED_KEYS_FIELD)
        {
            try
            {
                tmpSet = new NioSelectedKeySet();

                SELECTED_KEYS_FIELD.set(selector, tmpSet);
                PUBLIC_SELECTED_KEYS_FIELD.set(selector, tmpSet);
            }
            catch (final Exception ignore)
            {
                tmpSet = null;
            }
        }

        return tmpSet;
    }

    public static FileChannel createTmpFileChannel() throws IOException
    {
        final File file = File.createTempFile("buffer-", ".dat");
        file.deleteOnExit();

        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        final FileChannel fileChannel = randomAccessFile.getChannel();
        IoUtil.fill(fileChannel, 0, MTU_LENGTH_DEFAULT, (byte)0);

        return fileChannel;
    }
}
