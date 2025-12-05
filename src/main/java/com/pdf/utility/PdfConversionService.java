/**
 * 
 */
package com.pdf.utility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Sijo
 * 05-Dec-2025
 */
@Service
public class PdfConversionService {

    // ------------------------- PDF → TEXT -------------------------
    public String pdfToText(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        PDFTextStripper strip = new PDFTextStripper();
        String text = strip.getText(doc);
        doc.close();
        return text;
    }

    // ------------------------- PDF → WORD -------------------------
    public File pdfToWord(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        PDFTextStripper strip = new PDFTextStripper();
        String text = strip.getText(doc);
        doc.close();

        XWPFDocument word = new XWPFDocument();
        XWPFParagraph p = word.createParagraph();
        p.createRun().setText(text);

        File out = File.createTempFile("pdf-", ".docx");
        FileOutputStream fos = new FileOutputStream(out);
        word.write(fos);
        fos.close();
        word.close();
        return out;
    }

    // ------------------------- PDF → HTML -------------------------
    public File pdfToHtml(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        PDFTextStripper strip = new PDFTextStripper();
        String text = strip.getText(doc);
        doc.close();

        String html = "<html><body><pre>" + text + "</pre></body></html>";

        File out = File.createTempFile("pdf-", ".html");
        Files.write(out.toPath(), html.getBytes(StandardCharsets.UTF_8));
        return out;
    }

    // ------------------------- PDF → IMAGES -------------------------
    public List<File> pdfToImages(MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getInputStream());
        PDFRenderer renderer = new PDFRenderer(doc);

        List<File> images = new ArrayList<>();
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            BufferedImage img = renderer.renderImageWithDPI(i, 200);
            File imgFile = File.createTempFile("page-" + (i+1), ".png");
            ImageIO.write(img, "png", imgFile);
            images.add(imgFile);
        }

        doc.close();
        return images;
    }

    // ------------------------- IMAGE → PDF -------------------------
    public File imageToPdf(MultipartFile imageFile) throws Exception {
        BufferedImage img = ImageIO.read(imageFile.getInputStream());

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
        doc.addPage(page);

        PDImageXObject pdImage = LosslessFactory.createFromImage(doc, img);

        PDPageContentStream cs = new PDPageContentStream(doc, page);
        cs.drawImage(pdImage, 0, 0);
        cs.close();

        File out = File.createTempFile("img-", ".pdf");
        doc.save(out);
        doc.close();
        return out;
    }

    // ------------------------- WORD → PDF (simple) -------------------------
    public File wordToPdf(MultipartFile docx) throws Exception {
        XWPFDocument doc = new XWPFDocument(docx.getInputStream());

        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);

        PDFont font = PDType1Font.HELVETICA;
        PDPageContentStream cs = new PDPageContentStream(pdf, page);
        cs.setFont(font, 12);

        float y = 750;
        for (XWPFParagraph p : doc.getParagraphs()) {
            cs.beginText();
            cs.newLineAtOffset(50, y);
            cs.showText(p.getText());
            cs.endText();
            y -= 20;
        }

        cs.close();

        File out = File.createTempFile("word-", ".pdf");
        pdf.save(out);
        pdf.close();
        return out;
    }
}