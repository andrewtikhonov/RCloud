/*
 * R Cloud - R-based Cloud Platform for Computational Research
 * at EMBL-EBI (European Bioinformatics Institute)
 *
 * Copyright (C) 2007-2015 European Bioinformatics Institute
 * Copyright (C) 2009-2015 Andrew Tikhonov - andrew.tikhonov@gmail.com
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package workbench.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.server.RServices;
import uk.ac.ebi.rcloud.server.file.FileNode;

public class FileLoad {

    final private static Logger log = LoggerFactory.getLogger(FileLoad.class);

	private static final int BLOCK_SIZE_DOWNLOAD = 1024 * 16;
	private static final int BLOCK_SIZE_UPLOAD = 1024 * 16;

    private static int MAXRETRIES = 20;

	public static void download(String fromPath, File toFile, RServices r) throws RemoteException {
        int retries = MAXRETRIES;
        try {
			long fsize = r.getRandomAccessFileDescription(fromPath).length();
			RandomAccessFile raf = new RandomAccessFile(toFile.getAbsolutePath(), "rw");
			raf.setLength(0);
			while (raf.length() < fsize) {
				byte[] block = r.readRandomAccessFileBlock(fromPath, raf.length(), BLOCK_SIZE_DOWNLOAD);

                if (block == null) {
                    log.error("file transfer error, retrying..");

                    while(retries > 0 && block == null) {
                        try { Thread.sleep(500); } catch (Exception ex) {}
                        retries--;
                        log.error("attempt " + (MAXRETRIES - retries));
                        block = r.readRandomAccessFileBlock(fromPath, raf.length(), BLOCK_SIZE_DOWNLOAD);

                        if (block != null) break;
                    }

                    if (block != null) {
                        log.info("transfer successful.");
                    } else {
                        log.error("transfer unsuccessful " + fromPath);
                        raf.close();
                    }
                }
				raf.write(block);
			}
			raf.close();
		} catch (RemoteException re) {
			throw re;
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

	public static void upload(File fromFile, String toPath, RServices r) throws RemoteException {
		try {
			r.createRandomAccessFile(toPath);
			RandomAccessFile raf = new RandomAccessFile(fromFile.getAbsolutePath(), "r");
			long fsize = raf.length();
			raf.seek(0);
			while (raf.getFilePointer() < fsize) {
				byte[] block = new byte[BLOCK_SIZE_UPLOAD];
				int n = raf.read(block);
				if (n < block.length) {
					byte[] temp = new byte[n];
					System.arraycopy(block, 0, temp, 0, n);
					block = temp;
				}
				r.appendBlockToRandomAccessFile(toPath, block);
			}
			raf.close();
		} catch (RemoteException re) {
			throw re;
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}


    public static byte[] read(String fileName, RServices r) throws RemoteException {
        byte[] wholeThing = null;
        try {
            boolean absolute = fileName.contains(FileNode.separator);

            long fsize = absolute ? r.getRandomAccessFileDescription(fileName).length() :
                    r.getWorkingDirectoryFileDescription(fileName).length();

            int received = 0;
            int offset = 0;

            wholeThing = new byte[(int)fsize];

            while (received < fsize) {
                byte[] block = absolute ? r.readRandomAccessFileBlock(fileName, received, BLOCK_SIZE_DOWNLOAD) :
                        r.readWorkingDirectoryFileBlock(fileName, received, BLOCK_SIZE_DOWNLOAD);

                System.arraycopy(block, 0, wholeThing, offset, block.length);

                received += block.length;
                offset += block.length;
            }

        } catch (RemoteException re) {
            throw re;
        } catch (Exception e) {
            throw new RemoteException("", e);
        }
        return wholeThing;
    }

    public static void write(byte[] data, String fileName, RServices r) throws RemoteException {
        try {
            boolean absolute = fileName.contains(FileNode.separator);

            if (absolute)
                r.createRandomAccessFile(fileName);
            else
                r.createWorkingDirectoryFile(fileName);

            int fsize = data.length;
            int sent = 0;
            while (sent < fsize) {
                byte[] block = new byte[BLOCK_SIZE_UPLOAD];

                if (sent + block.length > fsize){
                    int size = fsize - sent;
                    byte[] temp = new byte[size];
                    block = temp;
                }
                System.arraycopy(data, sent, block, 0, block.length);

                if (absolute)
                    r.appendBlockToRandomAccessFile(fileName, block); 
                else
                    r.appendBlockToWorkingDirectoryFile(fileName, block);

                sent += block.length;
            }
        } catch (RemoteException re) {
            throw re;
        } catch (Exception e) {
            throw new RemoteException("", e);
        }
    }
}
