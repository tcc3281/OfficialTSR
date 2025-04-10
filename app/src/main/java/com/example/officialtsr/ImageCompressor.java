package com.example.officialtsr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageCompressor {

    public static File compressImage(Context context, Uri imageUri, float quality, int maxWidth) throws Exception {
        // Load the image as a Bitmap
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

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
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, (int) (quality * 100), outputStream);

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
