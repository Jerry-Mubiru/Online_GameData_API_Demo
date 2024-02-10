package dcmanagement.datamanagement.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;

import com.google.api.client.util.Value;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
@RequestMapping("/api/gamePlayData")
public class GameplayDataController {
    //stores the bucket name from dependencies
    @Value("$gcp.bucket.gameplay.name")
    private String gameplayBucketName;

    //get Method
    @GetMapping
    public ResponseEntity<InputStream> getData() throws IOException
    {
        //reference to google cloud storage service
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(gameplayBucketName, "gameplayData.txt");
        //check if the Blob is null
        if(blob==null)
        {
            return ResponseEntity.notFound().build();
        }
        //here blob has content
        InputStream content = new ByteArrayInputStream(blob.getContent());
        //read the stream into headers and content
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        //return the response with the file content in the body
        return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
    }
    
    //post method - initialize
    @PostMapping
    public ResponseEntity<String> initializeData(String newContent)
    {
        //Reference to the Storage service
        Storage storage = StorageOptions.getDefaultInstance().getService();
        //create a blob for the bucket
        Blob blob = storage.get(gameplayBucketName,"gamePlayData.txt");
        //if non existent - create else throw error
        if(blob == null)
        {
            BlobId identifiers = BlobId.of(gameplayBucketName,"gamePlayData.txt");
            BlobInfo info = BlobInfo.newBuilder(identifiers).build();
            storage.create(info, newContent.getBytes());
            return ResponseEntity.ok("Gameplay data initialized successfully!");
        }
        //at this point - the data exists
        return ResponseEntity.ok("User already exists");
    }

    //post method - update
    @PostMapping
    public ResponseEntity<String> updateData(String newContent)
    {
        //Reference to the Storage Service
        Storage storage = StorageOptions.getDefaultInstance().getService();
        //create a blob for the bucket
        Blob blob = storage.get(gameplayBucketName,"gamePlayData.txt");
        //if blob is null
        if(blob == null)
        {
            return ResponseEntity.notFound().build();
        }
        BlobInfo update = BlobInfo.newBuilder(gameplayBucketName, newContent)
                            .setContentType(blob.getContentType())
                            .setMetadata(blob.getMetadata())
                            .build();
        storage.create(update,newContent.getBytes());
        return ResponseEntity.ok("GamePlay Updated Successfully");
    }
}
