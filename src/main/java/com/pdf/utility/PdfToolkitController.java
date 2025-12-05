/**
 * 
 */
package com.pdf.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfToolkitController {

    @Autowired private PdfConversionService conversionService;
    @Autowired private PdfMergeService mergeService;
    @Autowired private PdfSplitService splitService;
    @Autowired private PdfCompressService compressService;
    @Autowired private PdfWatermarkService watermarkService;
    @Autowired private PdfSecurityService securityService;
    @Autowired private PdfMetadataService metadataService;

    // ------------------------------------------------------------
    // HELPER: Return single file
    // ------------------------------------------------------------
    private ResponseEntity<Resource> fileResponse(File file, String downloadName) throws Exception {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // ------------------------------------------------------------
    // HELPER: Return ZIP with multiple files
    // ------------------------------------------------------------
    private ResponseEntity<Resource> zipResponse(List<File> files, String zipName) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (File f : files) {
            FileInputStream fis = new FileInputStream(f);
            zipOut.putNextEntry(new ZipEntry(f.getName()));
            zipOut.write(fis.readAllBytes());
            zipOut.closeEntry();
            fis.close();
        }
        zipOut.close();

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // ============================================================
    // A. CONVERSION MODULE
    // ============================================================

    // 1. PDF → TEXT
    @PostMapping("/pdf-to-text")
    public ResponseEntity<String> pdfToText(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(conversionService.pdfToText(file));
    }

    // 2. PDF → WORD
    @PostMapping("/pdf-to-word")
    public ResponseEntity<Resource> pdfToWord(@RequestParam("file") MultipartFile file) throws Exception {
        File out = conversionService.pdfToWord(file);
        return fileResponse(out, "converted.docx");
    }

    // 3. PDF → HTML
    @PostMapping("/pdf-to-html")
    public ResponseEntity<Resource> pdfToHtml(@RequestParam("file") MultipartFile file) throws Exception {
        File out = conversionService.pdfToHtml(file);
        return fileResponse(out, "converted.html");
    }

    // 4. PDF → IMAGES (returns ZIP)
    @PostMapping("/pdf-to-images")
    public ResponseEntity<Resource> pdfToImages(@RequestParam("file") MultipartFile file) throws Exception {
        List<File> images = conversionService.pdfToImages(file);
        return zipResponse(images, "images.zip");
    }

    // 5. IMAGE → PDF
    @PostMapping("/image-to-pdf")
    public ResponseEntity<Resource> imageToPdf(@RequestParam("file") MultipartFile file) throws Exception {
        File out = conversionService.imageToPdf(file);
        return fileResponse(out, "image.pdf");
    }

    // 6. WORD → PDF
    @PostMapping("/word-to-pdf")
    public ResponseEntity<Resource> wordToPdf(@RequestParam("file") MultipartFile file) throws Exception {
        File out = conversionService.wordToPdf(file);
        return fileResponse(out, "converted.pdf");
    }

    // ============================================================
    // B. PDF MANIPULATION MODULE
    // ============================================================

    // 7. MERGE PDFs
    @PostMapping("/merge")
    public ResponseEntity<Resource> mergePdfs(@RequestParam("files") List<MultipartFile> files) throws Exception {
        File out = mergeService.merge(files);
        return fileResponse(out, "merged.pdf");
    }

    // 8. SPLIT PDF (returns ZIP)
    @PostMapping("/split")
    public ResponseEntity<Resource> splitPdf(@RequestParam("file") MultipartFile file) throws Exception {
        List<File> pages = splitService.split(file);
        return zipResponse(pages, "split_pages.zip");
    }

    // 9. COMPRESS PDF
    @PostMapping("/compress")
    public ResponseEntity<Resource> compressPdf(@RequestParam("file") MultipartFile file) throws Exception {
        File out = compressService.compress(file);
        return fileResponse(out, "compressed.pdf");
    }

    // 10. ADD TEXT WATERMARK
    @PostMapping("/watermark")
    public ResponseEntity<Resource> watermarkPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("text") String text) throws Exception {

        File out = watermarkService.addTextWatermark(file, text);
        return fileResponse(out, "watermarked.pdf");
    }

    // 11. PASSWORD PROTECT PDF
    @PostMapping("/protect")
    public ResponseEntity<Resource> protectPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws Exception {

        File out = securityService.protect(file, password);
        return fileResponse(out, "secured.pdf");
    }

    // 12. REMOVE PASSWORD
    @PostMapping("/unlock")
    public ResponseEntity<Resource> unlockPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws Exception {

        File out = securityService.removePassword(file, password);
        return fileResponse(out, "unlocked.pdf");
    }

    // 13. READ METADATA
    @PostMapping("/metadata/read")
    public ResponseEntity<Map<String, String>> readMetadata(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(metadataService.readMetadata(file));
    }

    // 14. WRITE METADATA
    @PostMapping("/metadata/write")
    public ResponseEntity<Resource> writeMetadata(
            @RequestParam("file") MultipartFile file,
            @RequestParam Map<String, String> meta) throws Exception {

        meta.remove("file");  // remove file param
        File out = metadataService.writeMetadata(file, meta);
        return fileResponse(out, "updated_metadata.pdf");
    }
}
