/**
 * 
 */
package com.pdf.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfSplitService {

    public List<File> split(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        Splitter splitter = new Splitter();

        List<PDDocument> pages = splitter.split(doc);
        List<File> output = new ArrayList<>();

        int i = 1;
        for (PDDocument page : pages) {
            File out = File.createTempFile("split-" + i, ".pdf");
            page.save(out);
            page.close();
            output.add(out);
            i++;
        }

        doc.close();
        return output;
    }
}
