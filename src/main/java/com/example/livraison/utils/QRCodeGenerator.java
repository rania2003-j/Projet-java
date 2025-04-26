package com.example.livraison.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Hashtable;

public class QRCodeGenerator {

    public static void generateQRCodeImage(String content, String filePath) throws WriterException, IOException {

        int width = 300;
        int height = 300;
        String fileType = "PNG";

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        Path path = FileSystems.getDefault().getPath(filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }

        MatrixToImageWriter.writeToPath(matrix, fileType, path);
    }


    public static void main(String[] args) {
        String livraisonInfo =
                "Livraison ID: 25\n" +
                        "Transporteur: Ahmed Ben Ali\n" +
                        "Voiture: Peugeot 208 (Matricule: 123TU456)\n" +
                        "État: En cours\n" +
                        "Date: 2025-04-24\n" +
                        "QR Code utilisé: Non";

        String outputPath = "qr_livraison_25.png";

        try {
            generateQRCodeImage(livraisonInfo, outputPath);
            System.out.println("✅ QR Code généré avec succès : " + outputPath);
        } catch (WriterException | IOException e) {
            System.err.println("❌ Erreur lors de la génération du QR Code : " + e.getMessage());
        }
    }
}




