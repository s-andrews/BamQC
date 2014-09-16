package uk.ac.babraham.BamQC.Utilities;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import net.iharder.Base64;

public class ImageToBase64 {

	public static String imageToBase64 (BufferedImage b) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		OutputStream b64 = new Base64.OutputStream(os);
		
		try {	
			ImageIO.write(b, "PNG", b64);
		
			return("data:image/png;base64,"+os.toString("UTF-8"));
		}
		catch (IOException e) {
			e.printStackTrace();
			return "Failed";
		}
		
	}
	
	
}
