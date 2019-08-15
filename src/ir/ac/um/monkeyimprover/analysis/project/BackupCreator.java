package ir.ac.um.monkeyimprover.analysis.project;

import com.intellij.openapi.vfs.VirtualFile;
import ir.ac.um.monkeyimprover.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupCreator {

    //    TODO  if it failed to create backup, the process should not continue
    public void createBackup(VirtualFile directory, List<VirtualFile> layoutFiles) {
        File backupDirectory = new File(directory.getCanonicalPath(), "backup");
        if (!backupDirectory.exists()) {
            boolean created = backupDirectory.mkdir();
            if(created) {
                Utils.showMessage("Backup directory created successfully.");
            } else {
                Utils.showMessage("Failed to create backup directory.");
            }
        }
        if (backupDirectory.exists()) {
            String timestamp = Utils.getTimestamp();
//            TODO use finally block
            try {
                File backupZipFile = new File(backupDirectory.getCanonicalPath(), "layout_backup_" + timestamp + ".zip");
                FileOutputStream fileOutputStream = new FileOutputStream(backupZipFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                for (VirtualFile layoutFile : layoutFiles) {
                    File file = new File(layoutFile.getCanonicalPath());
                    addFileToZipFile(zipOutputStream, file);
                }
                zipOutputStream.close();
            } catch (IOException e) {
                Utils.showException(e);
                e.printStackTrace();
            }

        }
    }

    private void addFileToZipFile(ZipOutputStream zipOutputStream, File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            byte[] buffer = new byte[1024];
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            Utils.showException(e);
            e.printStackTrace();
        }
    }

}
