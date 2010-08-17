/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package org.glassfish.hk2.classmodel.reflect.util;

import org.glassfish.hk2.classmodel.reflect.ArchiveAdapter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Directory base archive abstraction
 */
public class DirectoryArchive extends AbstractAdapter {
    public final File directory;

    public DirectoryArchive(File directory) {
        this.directory = directory;
    }

    @Override
    public URI getURI() {
        return directory.toURI();
    }


    @Override
    public Manifest getManifest() throws IOException {
        File manifest = new File(directory, JarFile.MANIFEST_NAME);
        if (manifest.exists()) {
            InputStream is = new BufferedInputStream(new FileInputStream(manifest));
            try {
                return new Manifest(is);
            } finally {
                is.close();
            }
        }
        return null;
    }

    @Override
    public void onSelectedEntries(Selector selector, EntryTask task, Logger logger) throws IOException {
        parse(directory, selector, task, logger);
    }

    private void parse(File dir, Selector selector, EntryTask task, Logger logger) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(52000);

        for (File f : dir.listFiles()) {
            Entry ae = new Entry(mangle(f), f.length(), f.isDirectory());
            if (!f.isDirectory()) {
                if (!selector.isSelected(ae))
                    continue;
                InputStream is = null;
                try {
                    try {
                        if (buffer.capacity()<f.length()) {
                            buffer = ByteBuffer.allocate((int) f.length());
                        }
                        FileChannel fc = (new FileInputStream(f)).getChannel();

                        int read = fc.read(buffer);
                        if (read!=f.length()) {
                             logger.severe("Incorrect file length while reading " + f.getName() +
                                     " of size " + f.length() + " reported is " + read);
                        }
                        buffer.rewind();
                        task.on(ae, buffer.array());
                    } catch(Exception e) {
                        logger.log(Level.SEVERE, "Exception while processing " + f.getName() +
                                " of size " + f.length(), e);
                    }

                } finally {
                    if (is!=null) {
                        is.close();
                    }
                }
            } else {
                parse(f, selector, task, logger);
            }

        }
    }

    private String mangle(File f) {
        String relativePath = f.getAbsolutePath().substring(directory.getAbsolutePath().length()+1);
        return relativePath.replace(File.separatorChar, '/');
    }

    @Override
    public void close() throws IOException {
    }
}
