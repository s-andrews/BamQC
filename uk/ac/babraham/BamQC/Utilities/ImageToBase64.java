package uk.ac.babraham.BamQC.Utilities;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.sourceforge.iharder.base64.Base64;

public class ImageToBase64 {

	private static Logger log = Logger.getLogger(ImageToBase64.class);
	
	public static String imageToBase64 (BufferedImage b) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		OutputStream b64 = new Base64.OutputStream(os);
		
		try {	
			ImageIO.write(b, "PNG", b64);
		
			return("data:image/png;base64,"+os.toString("UTF-8"));
		}
		catch (IOException e) {
			log.error("Failed", e);
			return "Failed";
		}
		
	}
	
	
}
