/**
 * 
 */
package com.pdf.utility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfCompressService {

    public File compress(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());

        for (PDPage page : doc.getPages()) {
            for (PDResources res : Collections.singleton(page.getResources())) {
                for (COSName name : res.getXObjectNames()) {
                    if (res.getXObject(name) instanceof PDImageXObject img) {
                        BufferedImage bi = img.getImage();
                        BufferedImage compressed = new BufferedImage(
                                bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
                        compressed.getGraphics().drawImage(bi, 0, 0, null);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(compressed, "jpg", baos);

                        PDImageXObject newImg = JPEGFactory.createFromByteArray(doc, baos.toByteArray());
                        res.put(name, newImg);
                    }
                }
            }
        }

        File out = File.createTempFile("compressed-", ".pdf");
        doc.save(out);
        doc.close();
        return out;
    }
}
