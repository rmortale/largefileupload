package ch.dulce.largefileupload.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.coyote.BadRequestException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/file")
public class FileController {

//    private final FileService fileService;
//
//    public FileController(FileService fileService) {
//        this.fileService = fileService;
//    }

    @PostMapping("/upload")
    public ResponseEntity<Void> upload(HttpServletRequest request) throws IOException {

        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            throw new BadRequestException("Multipart request expected");
        }
        JakartaServletFileUpload fileUpload = new JakartaServletFileUpload();

        // Parse the request
        fileUpload.getItemIterator(request).forEachRemaining(item -> {
            String name = item.getFieldName();
            InputStream stream = item.getInputStream();
            if (item.isFormField()) {
                System.out.println("Form field " + name + " with value "
                        + new String(stream.readAllBytes()) + " detected.");
            } else {
                System.out.println("File field " + name + " with file name "
                        + item.getName() + " detected.");
                // Process the input stream
                long start = System.currentTimeMillis();
                IOUtils.copyLarge(stream, new FileOutputStream(item.getName()));
                long end = System.currentTimeMillis();
                System.out.println("File uploaded in " + (end - start) + " ms");
            }
        });


        return ResponseEntity.status(HttpStatus.FOUND).build();
    }
}

