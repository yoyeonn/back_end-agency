package com.travel.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) {
    return uploadImage(file, "profiles");
  }

  // reusable method for any folder (hotels, rooms, etc.)
  public String uploadImage(MultipartFile file, String folder) {
    try {
      Map<?, ?> res = cloudinary.uploader().upload(
          file.getBytes(),
          Map.of("folder", folder)
      );
      return (String) res.get("secure_url");
    } catch (Exception e) {
      throw new RuntimeException("Cloudinary upload failed", e);
    }
  }

  public void deleteByUrl(String url) {
    String publicId = extractPublicId(url);
    if (publicId == null || publicId.isBlank()) return;

    try {
        cloudinary.uploader().destroy(publicId, Map.of());
    } catch (Exception e) {
        throw new RuntimeException("Cloudinary delete failed", e);
    }
}

private String extractPublicId(String url) {
    if (url == null || url.isBlank()) return null;

    Pattern p = Pattern.compile("/upload/(?:v\\d+/)?(.+?)\\.[a-zA-Z0-9]+$");
    Matcher m = p.matcher(url);
    if (!m.find()) return null;

    return m.group(1);
}
}
