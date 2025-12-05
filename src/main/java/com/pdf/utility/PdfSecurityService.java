/**
 * 
 */
package com.pdf.utility;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfSecurityService {

    public File protect(MultipartFile file, String password) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        AccessPermission perms = new AccessPermission();
        StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, perms);
        spp.setEncryptionKeyLength(128);
        doc.protect(spp);

        File out = File.createTempFile("secured-", ".pdf");
        doc.save(out);
        doc.close();
        return out;
    }

    public File removePassword(MultipartFile file, String password) throws Exception {
        // Load PDF with password
        PDDocument document = PDDocument.load(file.getInputStream(), password);

        // IMPORTANT: Remove security so it can be saved without encryption dictionary
        document.setAllSecurityToBeRemoved(true);

        File out = File.createTempFile("unlocked-", ".pdf");
        document.save(out);
        document.close();

        return out;
    }
}
