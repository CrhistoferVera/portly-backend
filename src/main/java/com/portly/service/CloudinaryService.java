package com.portly.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Sube una imagen y devuelve solo la URL segura.
     * Usado para avatares de perfil.
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "overwrite", true
                )
        );
        return (String) result.get("secure_url");
    }

    /**
     * Sube una imagen y devuelve metadatos extendidos:
     * url, thumbnailUrl, bytes, format, publicId.
     * Usado para evidencias de proyectos.
     */
    public Map<String, Object> uploadImageWithMetadata(MultipartFile file, String folder) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "use_filename", true,
                        "unique_filename", true
                )
        );

        String publicId  = (String) result.get("public_id");
        String secureUrl = (String) result.get("secure_url");
        Number bytes     = (Number) result.get("bytes");
        String format    = (String) result.get("format");

        // Genera URL de miniatura (150x150, relleno inteligente) manipulando la secure_url de Cloudinary.
        // Inserta /w_150,h_150,c_fill,g_auto/ antes del public_id en la URL.
        String thumbnailUrl = secureUrl.replace("/upload/", "/upload/w_150,h_150,c_fill,g_auto/");

        Map<String, Object> meta = new HashMap<>();
        meta.put("url",          secureUrl);
        meta.put("thumbnailUrl", thumbnailUrl);
        meta.put("bytes",        bytes != null ? bytes.longValue() : 0L);
        meta.put("format",       format != null ? format.toUpperCase() : "");
        meta.put("publicId",     publicId);
        return meta;
    }

    /**
     * Elimina una imagen de Cloudinary por su publicId.
     */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}

