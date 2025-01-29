package com.emt.dms1.testOCR;

import com.emt.dms1.document.DocumentModel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeGenerator {

    public static byte[] generateQRCode(DocumentModel documentModel) throws WriterException, IOException {
        String qrCodePath = "E:\\QR_Codes\\";
        String qrCodeName = qrCodePath + documentModel.getDocumentName() + documentModel.getId() + "-QRCODE.png";

        var qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                "ID: "+documentModel.getId()+ "\n"+
                        "Document name: "+documentModel.getDocumentName()+ "\n"+
                        "Department: "+documentModel.getDepartment()+ "\n"+
                        "Location: "+documentModel.getFileLocation()+ "\n"+
                        "Creation Date: "+documentModel.getCreateDate()+ "\n"+
                        "Created By: "+documentModel.getCreatedBy(), BarcodeFormat.QR_CODE, 300, 300);

        Path path = FileSystems.getDefault().getPath(qrCodeName);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }
}
