package de.saschat.reflectiontest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HTTPClassLoader extends ClassLoader {
    URL url;

    public HTTPClassLoader(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            InputStream stream = getData();
            byte[] data = locateClass(stream, name);
            return defineClass(name, data, 0, data.length);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        throw new ClassNotFoundException();
    }

    public static final byte[] CLASS_HEADER = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
    public static final byte[] ZIP_HEADER = new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04};

    public FileType getFileType(BufferedInputStream sr) {
        try {
            byte[] signature = sr.readNBytes(4);

            if (Arrays.equals(signature, CLASS_HEADER))
                return FileType.ClassFile;
            if (Arrays.equals(signature, ZIP_HEADER))
                return FileType.JarFile;


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return FileType.Unknown;
    }

    public static enum FileType {
        ClassFile,
        JarFile,
        Unknown
    }

    public InputStream getData() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        return conn.getInputStream();
    }

    public byte[] locateClass(InputStream stream, String name) throws ClassNotFoundException, IncorrectNameException {
        try {
            BufferedInputStream bif = new BufferedInputStream(stream);
            bif.mark(Integer.MAX_VALUE);
            FileType a = getFileType(bif);
            switch (a) {
                case JarFile -> {
                    bif.reset();
                    ZipInputStream zip = new ZipInputStream(bif);
                    ZipEntry ze;
                    while ((ze = zip.getNextEntry()) != null) {
                        if(ze.getName().endsWith(".class")) {
                            try {
                                return locateClass(zip, name);
                            } catch(Exception x) {
                                x.printStackTrace();
                            }
                        }
                    }
                }
                case ClassFile -> {
                    String className = getClassName(bif, true);
                    if(className.equals(name)) {
                        bif.reset();
                        return bif.readAllBytes();
                    } else {
                        throw new IncorrectNameException();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        throw new ClassNotFoundException();
    }

    public class IncorrectNameException extends Error {}


    public String getClassName(BufferedInputStream is, boolean magicRead) throws Exception {
        if(!magicRead) {
            FileType a = getFileType(is);
            if(a != FileType.ClassFile)
                throw new Exception("Not a class file.");
        }
        DataInputStream custom = new DataInputStream(is);
        short minVer = custom.readShort();
        short majVer = custom.readShort();

        int constantPool = custom.readShort() - 1;

        String[] cpStrings = new String[constantPool];
        int[] cpClasses = new int[constantPool];

        for (int i = 0; i < constantPool; i++) {
            int type = custom.read();
            switch (type) {
                case 0x01 -> {
                    cpStrings[i] = custom.readUTF();
                }
                case 0x05, 0x06 -> {
                    custom.readLong();
                    i++; // ??
                }
                case 0x07 -> {
                    cpClasses[i] = custom.readShort() & 0x0FF;
                }
                case 0x08 -> {
                    custom.readShort();
                }
                default -> {
                    custom.readInt();
                }
            }
        }
        short accessFlags = custom.readShort();
        short clazz = custom.readShort();
        return cpStrings[cpClasses[clazz-1]-1].replace("/", ".");
    }
    public String getClassName(BufferedInputStream is) throws Exception {
        return getClassName(is, false);
    }

}
