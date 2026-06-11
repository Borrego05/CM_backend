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

    // ── Informe de Servicios ──────────────────────────────────────────────────
    public void guardar_informe(String cliente, String codigo_informe,
                                List<byte[]> imagenes, List<byte[]> videos,
                                byte[] pdf_bytes)
    {
        String ruta_informe = normalizar(cliente) + "/" + codigo_informe + "/";
        guardar_documentacion(ruta_informe, imagenes, videos, pdf_bytes, "informe.pdf");
    }

    // ── Acta Mantenimiento de Silo ────────────────────────────────────────────
    public void guardar_acta(String cliente, String codigo_acta,
                             List<byte[]> imagenes, List<byte[]> videos,
                             byte[] pdf_bytes)
    {
        String ruta_acta = "actas/" + normalizar(cliente) + "/" + codigo_acta + "/";
        guardar_documentacion(ruta_acta, imagenes, videos, pdf_bytes, "acta.pdf");
    }

    // ── Lógica común: imágenes + videos + PDF en una ruta base ────────────────
    private void guardar_documentacion(String ruta_base,
                                       List<byte[]> imagenes, List<byte[]> videos,
                                       byte[] pdf_bytes, String nombre_pdf)
    {
        if (!enabled)
        {
            System.out.println("R2 deshabilitado en local — se omite la subida a: " + ruta_base);
            return;
        }

        if (existe_directorio(ruta_base))
        {
            throw new IllegalStateException(
                    "Ya existe un documento en la ruta " + ruta_base +
                    ". No se puede sobreescribir un registro existente.");
        }

        // Imágenes
        if (imagenes != null) {
            for (int i = 0; i < imagenes.size(); i++) {
                subir_archivo(ruta_base + "imagen_" + (i + 1) + ".jpg",
                        imagenes.get(i), "image/jpeg");
            }
        }

        // Videos
        if (videos != null) {
            for (int i = 0; i < videos.size(); i++) {
                subir_archivo(ruta_base + "video_" + (i + 1) + ".mp4",
                        videos.get(i), "video/mp4");
            }
        }

        // PDF
        subir_archivo(ruta_base + nombre_pdf, pdf_bytes, "application/pdf");
    }

    private String normalizar(String nombre)
    {
        return nombre.trim().toLowerCase().replaceAll("[^a-z0-9_\\-]", "_");
    }
}
