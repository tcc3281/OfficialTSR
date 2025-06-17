package com.example.officialtsr.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageCompressor {

    private static final String TAG = "ImageCompressor";

    public static File compressImage(Context context, Uri imageUri, float quality, int maxWidth) throws Exception {
        return compressImage(context, imageUri, quality, maxWidth, true);
    }

    public static File compressImage(Context context, Uri imageUri, float quality, int maxWidth, boolean flipHorizontal) throws Exception {
        // Load the image as a Bitmap
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            Log.e(TAG, "Failed to open InputStream for the provided URI: " + imageUri);
            throw new IllegalArgumentException("Failed to open InputStream for the provided URI.");
        }

        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (originalBitmap == null) {
            Log.e(TAG, "Failed to decode Bitmap from the provided URI: " + imageUri);
            throw new IllegalArgumentException("Failed to decode Bitmap from the provided URI. Unsupported format or corrupted file.");
        }

        // Flip horizontally if requested
        if (flipHorizontal) {
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1); // Flip horizontally (mirror effect)
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            Log.d(TAG, "Image flipped horizontally");
        }

        // Calculate new dimensions
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        float scale = Math.min((float) maxWidth / originalWidth, 1);
        int newWidth = Math.round(originalWidth * scale);
        int newHeight = Math.round(originalHeight * scale);

        // Resize the Bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

        // Compress the Bitmap
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean compressed = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, (int) (quality * 100), outputStream);

        if (!compressed) {
            Log.e(TAG, "Failed to compress the Bitmap.");
            throw new IllegalArgumentException("Failed to compress the Bitmap.");
        }

        // Save the compressed image to a temporary file
        File compressedFile = new File(context.getCacheDir(), "compressed_image.jpg");
        FileOutputStream fileOutputStream = new FileOutputStream(compressedFile);
        fileOutputStream.write(outputStream.toByteArray());
        fileOutputStream.close();

        // Recycle bitmaps to free memory
        originalBitmap.recycle();
        resizedBitmap.recycle();

        return compressedFile;
    }

    /**
     * Compress image specifically for AI object detection and classification
     * Maintains high quality and proper orientation for accurate AI processing
     */
    public static File compressImageForAI(Context context, Uri imageUri) throws Exception {
        return compressImageForAI(context, imageUri, false); // Don't flip for AI by default
    }

    public static File compressImageForAI(Context context, Uri imageUri, boolean flipHorizontal) throws Exception {
        // Load the image as a Bitmap
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            Log.e(TAG, "Failed to open InputStream for the provided URI: " + imageUri);
            throw new IllegalArgumentException("Failed to open InputStream for the provided URI.");
        }

        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (originalBitmap == null) {
            Log.e(TAG, "Failed to decode Bitmap from the provided URI: " + imageUri);
            throw new IllegalArgumentException("Failed to decode Bitmap from the provided URI.");
        }

        // Handle orientation correction based on EXIF data
        originalBitmap = correctImageOrientation(context, imageUri, originalBitmap);        // Flip horizontally if requested (needed to correct camera orientation for traffic signs)
        if (flipHorizontal) {
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, 
                originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            Log.d(TAG, "Image flipped horizontally to correct direction for traffic signs");
        }

        // For AI processing, maintain higher resolution
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        
        // AI optimal settings: keep resolution high but limit to reasonable size
        int maxDimension = 1280; // Good balance for object detection
        float scale = 1.0f;
        
        // Only resize if image is too large
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            scale = Math.min((float) maxDimension / originalWidth, 
                            (float) maxDimension / originalHeight);
            Log.d(TAG, "Resizing for AI: scale = " + scale);
        }
        
        Bitmap processedBitmap = originalBitmap;
        
        if (scale < 1.0f) {
            int newWidth = Math.round(originalWidth * scale);
            int newHeight = Math.round(originalHeight * scale);
            processedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            Log.d(TAG, "Resized to: " + newWidth + "x" + newHeight);
        }

        // Use high-quality JPEG compression for AI
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int quality = 92; // High quality for AI processing
        boolean compressed = processedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        if (!compressed) {
            Log.e(TAG, "Failed to compress the Bitmap for AI processing.");
            throw new IllegalArgumentException("Failed to compress the Bitmap for AI processing.");
        }

        // Save to temporary file with unique name
        String fileName = "ai_image_" + System.currentTimeMillis() + ".jpg";
        File compressedFile = new File(context.getCacheDir(), fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(compressedFile);
        fileOutputStream.write(outputStream.toByteArray());
        fileOutputStream.close();

        Log.d(TAG, "AI image saved: " + compressedFile.getAbsolutePath() + 
              " Size: " + (compressedFile.length() / 1024) + "KB");

        // Cleanup
        if (processedBitmap != originalBitmap) {
            originalBitmap.recycle();
        }
        processedBitmap.recycle();

        return compressedFile;
    }

    /**
     * Correct image orientation based on EXIF data
     */
    private static Bitmap correctImageOrientation(Context context, Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                android.media.ExifInterface exif = new android.media.ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 
                    android.media.ExifInterface.ORIENTATION_NORMAL);
                
                Matrix matrix = new Matrix();
                switch (orientation) {
                    case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        Log.d(TAG, "Rotating image 90 degrees");
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        Log.d(TAG, "Rotating image 180 degrees");
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        Log.d(TAG, "Rotating image 270 degrees");
                        break;
                    case android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                        matrix.postScale(-1, 1);
                        Log.d(TAG, "Flipping image horizontally");
                        break;
                    default:
                        Log.d(TAG, "No orientation correction needed");
                        return bitmap; // No correction needed
                }
                
                inputStream.close();
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading EXIF data: " + e.getMessage());
        }
        return bitmap; // Return original if EXIF reading fails
    }
}
