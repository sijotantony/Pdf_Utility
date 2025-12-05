/**
 * 
 */
package com.pdf.utility;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfMetadataService {

    public Map<String, String> readMetadata(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        PDDocumentInformation info = doc.getDocumentInformation();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Title", info.getTitle());
        map.put("Author", info.getAuthor());
        map.put("Subject", info.getSubject());
        map.put("Keywords", info.getKeywords());
        map.put("Creator", info.getCreator());
        map.put("Producer", info.getProducer());

        doc.close();
        return map;
    }

    public File writeMetadata(MultipartFile file, Map<String, String> meta) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        PDDocumentInformation info = doc.getDocumentInformation();

        meta.forEach(info::setCustomMetadataValue);

        File out = File.createTempFile("meta-", ".pdf");
        doc.save(out);
        doc.close();
        return out;
    }
}
