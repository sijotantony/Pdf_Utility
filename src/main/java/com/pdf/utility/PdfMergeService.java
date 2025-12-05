/**
 * 
 */
package com.pdf.utility;

import java.io.File;
import java.util.List;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfMergeService {

    public File merge(List<MultipartFile> pdfFiles) throws Exception {
        PDFMergerUtility util = new PDFMergerUtility();

        File out = File.createTempFile("merged-", ".pdf");
        util.setDestinationFileName(out.getAbsolutePath());

        for (MultipartFile f : pdfFiles) {
            util.addSource(f.getInputStream());
        }
        util.mergeDocuments(null);

        return out;
    }
}
