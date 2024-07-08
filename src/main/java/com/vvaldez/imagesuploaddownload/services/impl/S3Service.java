package com.vvaldez.imagesuploaddownload.services.impl;

import com.vvaldez.imagesuploaddownload.services.IS3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service implements IS3Service {

    @Value("${upload.s3.localPath}")
    private String localPath;
    @Value("${aws.bucket}")
    private String bucketName;
    private S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        try{
            String fileName = file.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return "Archivo subido correctamente";

        }catch (IOException e){
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String downloadFile(String fileName) throws IOException {
        if(!doesObjectExists(fileName)){
            return  "El archivo introducido no existe";
        }
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        ResponseInputStream<GetObjectResponse> result = s3Client.getObject(request);
        try(FileOutputStream fos = new FileOutputStream(localPath+fileName)){
            byte[] readBuf = new byte[1024];
            int readLen = 0;
            while((readLen = result.read(readBuf))> 0){
                fos.write(readBuf, 0, readLen);
            }

        }catch (IOException e){
            throw new IOException(e.getMessage());
        }
        return "Archivo descargado correctamente";
    }

    @Override
    public List<String> listFiles() throws IOException {
        try{
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .build();
            List<S3Object> objects = s3Client.listObjects(listObjectsRequest).contents();
            List<String> fileNames = new ArrayList<>();

            for(S3Object object: objects){
                fileNames.add(object.key());
            }
            return fileNames;

        }catch (S3Exception e){
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String renameFile(String oldFileName, String newFileName) throws IOException {
        if(!doesObjectExists(oldFileName)){
            return "El archivo introducido no existe";
        }

        try{
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .destinationBucket(bucketName)
                    .sourceBucket(bucketName)
                    .sourceKey(oldFileName)
                    .destinationKey(newFileName)
                    .build();
            s3Client.copyObject(copyObjectRequest);
            deleteFile(oldFileName);
            return "El archivo fue renombrado correctamente";

        }catch (S3Exception e){
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String updateFile(MultipartFile file, String oldFileName) throws IOException {
        if(!doesObjectExists(oldFileName)){
            return "El archivo introducido no existe";
        }

        try{
            String newFileName = file.getOriginalFilename();
            deleteFile(oldFileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(newFileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return "El archivo fue modificado correctamente";

        }catch (S3Exception e){
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String deleteFile(String fileName) throws IOException {
        if(!doesObjectExists(fileName)){
            return "El archivo introducido no existe";
        }
        try{
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            return "El archivo fue borrado correctamente";

        }catch (S3Exception e){
            throw new IOException(e.getMessage());
        }

    }

    private boolean doesObjectExists(String objectKey){
        try{
            HeadObjectRequest headObjectRequest = HeadObjectRequest
                    .builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.headObject(headObjectRequest);


        }catch (S3Exception e){
            if(e.statusCode() == 404){
                return false;
            }
        }
        return true;
    }
}
