package com.controlmezcla.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

@Service
public class R2StorageService {

    private final S3Client s3Client;

    @Value("${R2_BUCKET}")
    private String bucket;

    @Value("${r2.enabled}")
    private boolean enabled;

    public R2StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private boolean existe_directorio(String prefijo)
    {
        String prefijo_normalizado = prefijo.endsWith("/") ? prefijo : prefijo + "/";
        ListObjectsV2Response response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .prefix(prefijo_normalizado)
                        .maxKeys(1)
                        .build()
        );
        return !response.contents().isEmpty();
    }

    private void subir_archivo(String ruta, byte[] contenido, String contentType)
    {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(ruta)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(contenido)
        );
    }

    public void guardar_informe(String cliente, String codigo_informe, List<byte[]> imagenes, byte[] pdf_bytes)
    {
        String cliente_normalizado = cliente.trim().toLowerCase()
                .replaceAll("[^a-z0-9_\\-]", "_");
        String ruta_cliente = cliente_normalizado + "/";
        String ruta_informe = cliente_normalizado + "/" + codigo_informe + "/";

        if(!enabled)
        {
            System.out.println("R2 deshabilitado en ambiente de pruebas local - Se salta la subida de archivos");
            return;
        }

        if(existe_directorio(ruta_informe))
        {
            throw new IllegalStateException(
                    "Ya existe un informe con el codigo " +
                            codigo_informe + "para el cliente " + cliente +
                            ". " + "No se puede reescribir un informe existente."
            );
        }

        for(int i = 0; i < imagenes.size(); i++)
        {
            String ruta_imagen = ruta_informe + "imagen_" + (i + 1) + ".jpg";
            subir_archivo(ruta_imagen, imagenes.get(i), "image/jpeg");
        }

        String ruta_pdf = ruta_informe + "informe.pdf";
        subir_archivo(ruta_pdf, pdf_bytes, "application/pdf");
    }
}
