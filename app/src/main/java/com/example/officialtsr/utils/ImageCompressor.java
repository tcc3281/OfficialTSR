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
}
