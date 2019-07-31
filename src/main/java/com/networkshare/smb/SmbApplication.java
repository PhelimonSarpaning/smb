package com.networkshare.smb;

import jcifs.smb.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class SmbApplication {
    private String USER_NAME = "";
    private String PASSWORD = "";
    private String DOMAIN = "";
    private String NETWORK_FOLDER = "smb://";

    public static void main(String args[]) {
        SpringApplication.run(SmbApplication.class, args);
        try {
            new SmbApplication().auth();
        } catch (Exception e) {
            System.err.println("Exception caught. Cause: " + e.getMessage());
        }
    }

    public boolean auth() throws IOException {
        boolean successful = false;
        String path = null;
        NtlmPasswordAuthentication auth = null;
        SmbFile sFile = null;
        SmbFileOutputStream sfos = null;
        SmbFileInputStream in = null;

        try {
            auth = new NtlmPasswordAuthentication( DOMAIN, USER_NAME, PASSWORD);
            path = NETWORK_FOLDER;
            sFile = new SmbFile(path, auth);
            SmbFile[] files = sFile.listFiles();

            String fileName = "smb://";
            SmbFile dir = new SmbFile(fileName, auth);
            System.out.println(dir.getDate());

            try{
                 in = new SmbFileInputStream(dir);
               //new SmbApplication().compress(in);
                new SmbApplication().encoder(dir, in);

            }
            catch (Exception e){
                System.err.println("Unable to encode file. Cause: "
                        + e.getMessage());
            }

            successful = true;

        } catch (Exception e) {
            successful = false;
            System.err.println("Unable to authenticate. Cause: "
                    + e.getMessage());
        }
        in.close();
        return successful;
    }

    public Set<SmbFile> listFiles(final SmbFile directory, final boolean recurseSubdirectories) throws SmbException {
        if (!directory.canRead() || !directory.canWrite()) {
            throw new IllegalStateException("Directory '" + directory + "' does not have sufficient permissions.");
        }
        final Set<SmbFile> queue = new HashSet<SmbFile>();
        if (!directory.exists()) {
            return queue;
        }

        final SmbFile[] children = directory.listFiles();
        if (children == null) {
            return queue;
        }

        for (final SmbFile child : children) {
            if (child.isDirectory()) {
                if (recurseSubdirectories) {
                    queue.addAll(listFiles(child, recurseSubdirectories));
                }
            } else  {
                queue.add(child);
            }
        }

        return queue;
    }

    //image encoder
    public static String encoder(SmbFile file, SmbFileInputStream imageInFile) {
        String base64Image = "";
        try  {

            // Reading a Image file from file system
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
            base64Image = Base64.getEncoder().encodeToString(imageData);

        } catch (FileNotFoundException e) {

            System.out.println("Image not found" + e);

        } catch (IOException ioe) {

            System.out.println("Exception while reading the Image " + ioe);

        }

        return base64Image;
    }



}
