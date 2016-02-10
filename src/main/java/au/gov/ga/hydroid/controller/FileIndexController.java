package au.gov.ga.hydroid.controller;

import au.gov.ga.hydroid.HydroidConfiguration;
import au.gov.ga.hydroid.dto.ServiceResponse;
import au.gov.ga.hydroid.service.EnhancerService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

/**
 * Created by Layoric on 9/02/2016.
 */
@RestController
@RequestMapping("/index-file")
public class FileIndexController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EnhancerService enhancerService;

    @Autowired
    private HydroidConfiguration configuration;

    @RequestMapping(value="", method = {RequestMethod.POST})
    public @ResponseBody
    ResponseEntity<ServiceResponse> handleFileUpload(@RequestParam("name") String name,
                            @RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                PDDocument doc = PDDocument.load(byteArrayInputStream);
                String text = pdfTextStripper.getText(doc);
                enhancerService.enhance(configuration.getStanbolChain(),
                        name,
                        text,
                        configuration.getSolrCollection());
            } catch (Exception e) {
                throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed extracting/indexing text from file");
            }
        } else {
            return new ResponseEntity<ServiceResponse>(
                    new ServiceResponse("You failed to upload " + name + " because the file was empty."),HttpStatus.OK);
        }

        return new ResponseEntity<ServiceResponse>(new ServiceResponse("Your document has been enhanced successfully."),
                HttpStatus.OK);
    }
}
