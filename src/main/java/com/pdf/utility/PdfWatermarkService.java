/**
 * 
 */
package com.pdf.utility;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfWatermarkService {

    public File addTextWatermark(MultipartFile file, String watermark) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());

        for (PDPage page : doc.getPages()) {
            PDPageContentStream cs = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 50);
            cs.setNonStrokingColor(200, 200, 200);
            cs.beginText();
            cs.setTextRotation(Math.toRadians(45), 200, 200);
            cs.showText(watermark);
            cs.endText();
            cs.close();
        }

        File out = File.createTempFile("watermark-", ".pdf");
        doc.save(out);
        doc.close();
        return out;
    }
}