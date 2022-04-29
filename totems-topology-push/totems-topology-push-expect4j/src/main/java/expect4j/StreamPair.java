/*
 * Copyright (c) 2007 Justin Ryan
 * Copyright (c) 2013 Chris Verges <chris.verges@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package expect4j;

import java.io.*;

/**
 * TODO
 *
 * @author Chris Verges
 * @author Justin Ryan
 */
public class StreamPair implements IOPair {
    Reader is;
    Writer os;

    /** Creates a new instance of ReaderConsumer */
    public StreamPair(InputStream is, OutputStream os ) {
        this.is = new InputStreamReader(is );
        this.os = new OutputStreamWriter( os );
    }

    /**
     * Creates a new instance of ReaderConsumer
     * @param is InputStream
     * @param os OutputStream
     * @param isCharsetName charsetName The name of a supported {@link java.nio.charset.Charset charset}
     * @param osCharsetName charsetName The name of a supported {@link java.nio.charset.Charset charset}
     * @throws UnsupportedEncodingException
     */
    public StreamPair(InputStream is, OutputStream os, String isCharsetName, String osCharsetName) throws UnsupportedEncodingException {
        this.is = new InputStreamReader(is, isCharsetName);
        this.os = new OutputStreamWriter(os, osCharsetName);
    }

    @Override
    public Reader getReader() {
        return is;
    }

    @Override
    public Writer getWriter() {
        return os;
    }

    /**
     * TODO evaluate if this is even needed
     */
    @Override
    public void reset() {
        try {
            is.reset();
        }catch(IOException ioe) {
        }
    }

    @Override
    public void close() {
        try { is.close(); } catch(Exception e) { }
        try { os.close(); } catch(Exception e) { }
    }
}
