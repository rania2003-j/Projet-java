package com.example.livraison.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.awt.image.BufferedImage;

public class QRCodeScanner {

    private QRCodeReader qrReader;
    private boolean isUsed = false;

    public QRCodeScanner() {
        this.qrReader = new QRCodeReader();
    }

    public String scanQRCode(BufferedImage image) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Result result = qrReader.decode(bitmap);

            if (result != null) {
                String qrCodeData = result.getText();


                if (isUsed) {
                    return "Ce QR code a déjà été utilisé.";
                }


                isUsed = true;

                return qrCodeData;
            }
        } catch (NotFoundException e) {
            return "Aucun QR code trouvé.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'analyse du QR code.";
        }
        return "Erreur inconnue.";
    }

    public void resetScan() {
        isUsed = false;
    }
}

















